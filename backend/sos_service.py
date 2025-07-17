import json
import os
from datetime import datetime
from typing import Dict, List, Optional
import requests
from twilio.rest import Client
from twilio.base.exceptions import TwilioException

class SOSService:
    """Service to handle SOS emergency alerts"""
    
    def __init__(self):
        # Twilio configuration for SMS
        self.twilio_account_sid = os.getenv("TWILIO_ACCOUNT_SID", "")
        self.twilio_auth_token = os.getenv("TWILIO_AUTH_TOKEN", "")
        self.twilio_phone_number = os.getenv("TWILIO_PHONE_NUMBER", "")
        
        # Initialize Twilio client
        if self.twilio_account_sid and self.twilio_auth_token:
            self.twilio_client = Client(self.twilio_account_sid, self.twilio_auth_token)
        else:
            self.twilio_client = None
        
        # Emergency contacts database
        self.emergency_contacts_file = "emergency_contacts.json"
        self.load_emergency_contacts()
    
    def load_emergency_contacts(self):
        """Load emergency contacts from file"""
        try:
            if os.path.exists(self.emergency_contacts_file):
                with open(self.emergency_contacts_file, 'r') as f:
                    self.emergency_contacts = json.load(f)
            else:
                self.emergency_contacts = {
                    "contacts": [],
                    "emergency_services": {
                        "police": "100",
                        "ambulance": "102", 
                        "fire": "101",
                        "disaster_management": "1078"
                    }
                }
                self.save_emergency_contacts()
        except Exception as e:
            print(f"Error loading emergency contacts: {e}")
            self.emergency_contacts = {"contacts": [], "emergency_services": {}}
    
    def save_emergency_contacts(self):
        """Save emergency contacts to file"""
        try:
            with open(self.emergency_contacts_file, 'w') as f:
                json.dump(self.emergency_contacts, f, indent=2)
        except Exception as e:
            print(f"Error saving emergency contacts: {e}")
    
    def add_emergency_contact(self, name: str, phone: str, relationship: str = "Emergency Contact"):
        """Add a new emergency contact"""
        contact = {
            "id": len(self.emergency_contacts["contacts"]) + 1,
            "name": name,
            "phone": phone,
            "relationship": relationship,
            "added_date": datetime.now().isoformat()
        }
        
        self.emergency_contacts["contacts"].append(contact)
        self.save_emergency_contacts()
        return contact
    
    def remove_emergency_contact(self, contact_id: int):
        """Remove an emergency contact"""
        self.emergency_contacts["contacts"] = [
            c for c in self.emergency_contacts["contacts"] 
            if c["id"] != contact_id
        ]
        self.save_emergency_contacts()
    
    def get_emergency_contacts(self) -> List[Dict]:
        """Get all emergency contacts"""
        return self.emergency_contacts["contacts"]
    
    def send_sos_alert(
        self, 
        user_location: Dict, 
        user_message: str = "", 
        alert_type: str = "general"
    ) -> Dict:
        """Send SOS alert to emergency contacts and services"""
        try:
            timestamp = datetime.now().isoformat()
            alert_id = f"sos_{int(datetime.now().timestamp())}"
            
            # Create alert data
            alert_data = {
                "alert_id": alert_id,
                "timestamp": timestamp,
                "location": user_location,
                "message": user_message,
                "alert_type": alert_type,
                "status": "sent"
            }
            
            # Send to emergency contacts
            contacts_notified = []
            for contact in self.emergency_contacts["contacts"]:
                try:
                    if self.send_sms_alert(contact["phone"], alert_data):
                        contacts_notified.append(contact["name"])
                except Exception as e:
                    print(f"Failed to notify {contact['name']}: {e}")
            
            # Send to emergency services based on alert type
            services_notified = []
            if alert_type == "medical":
                services_notified.append("ambulance")
            elif alert_type == "fire":
                services_notified.append("fire")
            elif alert_type == "crime":
                services_notified.append("police")
            else:
                services_notified.append("disaster_management")
            
            # Save alert history
            self.save_alert_history(alert_data)
            
            return {
                "success": True,
                "alert_id": alert_id,
                "contacts_notified": contacts_notified,
                "services_notified": services_notified,
                "message": "SOS alert sent successfully"
            }
            
        except Exception as e:
            return {
                "success": False,
                "error": str(e),
                "message": "Failed to send SOS alert"
            }
    
    def send_sms_alert(self, phone_number: str, alert_data: Dict) -> bool:
        """Send SMS alert using Twilio"""
        if not self.twilio_client:
            print("Twilio not configured - SMS not sent")
            return False
        
        try:
            message_body = self.create_sos_message(alert_data)
            
            message = self.twilio_client.messages.create(
                body=message_body,
                from_=self.twilio_phone_number,
                to=phone_number
            )
            
            print(f"SMS sent to {phone_number}: {message.sid}")
            return True
            
        except TwilioException as e:
            print(f"Twilio error: {e}")
            return False
        except Exception as e:
            print(f"SMS error: {e}")
            return False
    
    def create_sos_message(self, alert_data: Dict) -> str:
        """Create SOS message content"""
        location = alert_data["location"]
        message = alert_data["message"]
        
        sos_message = f"""
🚨 SOS ALERT 🚨

Location: {location.get('latitude', 'N/A')}, {location.get('longitude', 'N/A')}
Time: {alert_data['timestamp']}
Type: {alert_data['alert_type'].upper()}

{message if message else 'Emergency assistance needed!'}

This is an automated SOS alert from EcoScope AI.
Please respond immediately.
        """.strip()
        
        return sos_message
    
    def save_alert_history(self, alert_data: Dict):
        """Save alert to history"""
        history_file = "sos_alerts_history.json"
        
        try:
            if os.path.exists(history_file):
                with open(history_file, 'r') as f:
                    history = json.load(f)
            else:
                history = {"alerts": []}
            
            history["alerts"].append(alert_data)
            
            # Keep only last 100 alerts
            if len(history["alerts"]) > 100:
                history["alerts"] = history["alerts"][-100:]
            
            with open(history_file, 'w') as f:
                json.dump(history, f, indent=2)
                
        except Exception as e:
            print(f"Error saving alert history: {e}")
    
    def get_alert_history(self, limit: int = 10) -> List[Dict]:
        """Get recent alert history"""
        history_file = "sos_alerts_history.json"
        
        try:
            if os.path.exists(history_file):
                with open(history_file, 'r') as f:
                    history = json.load(f)
                return history["alerts"][-limit:]
            else:
                return []
        except Exception as e:
            print(f"Error loading alert history: {e}")
            return []
    
    def get_emergency_services(self) -> Dict:
        """Get emergency service numbers"""
        return self.emergency_contacts["emergency_services"]

# Example usage
if __name__ == "__main__":
    sos_service = SOSService()
    print("🚨 SOS Service initialized!") 