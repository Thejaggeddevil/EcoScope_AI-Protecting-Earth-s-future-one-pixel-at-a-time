from fastapi import FastAPI, File, UploadFile, HTTPException, Form
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import uvicorn
import os
import sys
import json
from datetime import datetime
from typing import Dict, List, Optional
from dotenv import load_dotenv

# Add parent directory to path for model imports
sys.path.append(os.path.dirname(os.path.dirname(__file__)))

# Import the EcoScope model and satellite service
from ecoscope_model import EcoScopeModel
from satellite_service import SatelliteService
from sos_service import SOSService

# Load environment variables from .env file
load_dotenv()
COPERNICUS_API_KEY = os.getenv("COPERNICUS_API_KEY")
# Now you can use COPERNICUS_API_KEY wherever you need to call Copernicus APIs

app = FastAPI(
    title="EcoScope AI API",
    description="Environmental Analysis API for Glaciers, Drainage Systems, and Road Networks",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize the model and satellite service
model = None
satellite_service = None
sos_service = None

@app.on_event("startup")
async def startup_event():
    """Initialize the EcoScope model and satellite service on startup"""
    global model, satellite_service, sos_service
    try:
        model = EcoScopeModel()
        satellite_service = SatelliteService()
        sos_service = SOSService()
        print("🌿 EcoScope AI Model initialized successfully!")
        print("🛰️ Satellite Service initialized successfully!")
        print("🚨 SOS Service initialized successfully!")
    except Exception as e:
        print(f"❌ Error initializing services: {e}")
        model = None
        satellite_service = None
        sos_service = None

@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "message": "EcoScope AI API",
        "version": "1.0.0",
        "status": "running",
        "model_loaded": model is not None
    }

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "model_status": "loaded" if model is not None else "not_loaded"
    }

@app.post("/analyze/environmental")
async def analyze_environmental_changes(file: UploadFile = File(...)):
    """Analyze environmental changes in uploaded image"""
    if model is None:
        raise HTTPException(status_code=500, detail="Model not loaded")
    
    # Validate file type
    if not file.content_type.startswith('image/'):
        raise HTTPException(status_code=400, detail="File must be an image")
    
    try:
        # Read image data
        image_data = await file.read()
        
        # Analyze the image
        analysis = model.analyze_environmental_changes(image_data)
        
        # Save analysis result
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_path = f"analysis_results_{timestamp}.json"
        model.save_analysis_result(analysis, output_path)
        
        return JSONResponse(content=analysis)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")

@app.post("/analyze/glaciers")
async def analyze_glaciers(file: UploadFile = File(...)):
    """Specific analysis for glacier detection"""
    if model is None:
        raise HTTPException(status_code=500, detail="Model not loaded")
    
    try:
        image_data = await file.read()
        analysis = model.analyze_environmental_changes(image_data)
        
        # Extract glacier-specific information
        glacier_analysis = {
            "timestamp": analysis["timestamp"],
            "glacier_detection": analysis["features_detected"]["glaciers"],
            "impact_level": analysis["impact_level"],
            "recommendations": [rec for rec in analysis["recommendations"] 
                              if "glacier" in rec.lower() or "water" in rec.lower()]
        }
        
        return JSONResponse(content=glacier_analysis)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Glacier analysis failed: {str(e)}")

@app.post("/analyze/drainage")
async def analyze_drainage_systems(file: UploadFile = File(...)):
    """Specific analysis for drainage systems"""
    if model is None:
        raise HTTPException(status_code=500, detail="Model not loaded")
    
    try:
        image_data = await file.read()
        analysis = model.analyze_environmental_changes(image_data)
        
        # Extract drainage-specific information
        drainage_analysis = {
            "timestamp": analysis["timestamp"],
            "drainage_detection": analysis["features_detected"]["drainage_systems"],
            "impact_level": analysis["impact_level"],
            "recommendations": [rec for rec in analysis["recommendations"] 
                              if "drainage" in rec.lower() or "flooding" in rec.lower()]
        }
        
        return JSONResponse(content=drainage_analysis)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Drainage analysis failed: {str(e)}")

@app.post("/analyze/roads")
async def analyze_road_networks(file: UploadFile = File(...)):
    """Specific analysis for road networks"""
    if model is None:
        raise HTTPException(status_code=500, detail="Model not loaded")
    
    try:
        image_data = await file.read()
        analysis = model.analyze_environmental_changes(image_data)
        
        # Extract road-specific information
        road_analysis = {
            "timestamp": analysis["timestamp"],
            "road_detection": analysis["features_detected"]["road_networks"],
            "impact_level": analysis["impact_level"],
            "recommendations": [rec for rec in analysis["recommendations"] 
                              if "infrastructure" in rec.lower() or "habitat" in rec.lower()]
        }
        
        return JSONResponse(content=road_analysis)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Road analysis failed: {str(e)}")

@app.get("/model/info")
async def get_model_info():
    """Get information about the loaded model"""
    if model is None:
        raise HTTPException(status_code=500, detail="Model not loaded")
    
    return {
        "model_type": "EcoScope AI - Environmental Analysis",
        "capabilities": [
            "Glacier detection and melting analysis",
            "Urban drainage system mapping",
            "Road network infrastructure analysis",
            "Environmental impact assessment",
            "Before/after change detection"
        ],
        "input_format": "Image files (PNG, JPG, JPEG)",
        "output_format": "JSON analysis results",
        "device": model.device,
        "image_size": model.image_size
    }

@app.get("/analysis/history")
async def get_analysis_history():
    """Get list of recent analysis results"""
    try:
        analysis_files = [f for f in os.listdir(".") if f.startswith("analysis_results_")]
        analysis_files.sort(reverse=True)  # Most recent first
        
        history = []
        for file in analysis_files[:10]:  # Last 10 analyses
            try:
                with open(file, 'r') as f:
                    data = json.load(f)
                    history.append({
                        "file": file,
                        "timestamp": data.get("timestamp", ""),
                        "impact_level": data.get("impact_level", ""),
                        "affected_area": data.get("affected_area_percentage", 0)
                    })
            except:
                continue
        
        return {"analysis_history": history}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get history: {str(e)}")

# ===== Satellite Analysis Endpoints =====

@app.post("/satellite/compare")
async def compare_satellite_images(
    latitude: float = Form(...),
    longitude: float = Form(...),
    start_date: str = Form(...),
    end_date: str = Form(...)
):
    """Compare satellite images from different dates for environmental changes"""
    if satellite_service is None:
        raise HTTPException(status_code=500, detail="Satellite service not available")
    
    try:
        # Fetch satellite images
        images_result = satellite_service.get_satellite_images(
            latitude, longitude, start_date, end_date
        )
        
        if not images_result["success"]:
            raise HTTPException(status_code=400, detail=images_result["error"])
        
        # Analyze changes using AI model if available
        if model is not None:
            # Use AI model to analyze the satellite images
            analysis_result = await analyze_satellite_changes(images_result["images"])
        else:
            analysis_result = {"ai_analysis": "Model not available"}
        
        # Combine results
        result = {
            "timestamp": datetime.now().isoformat(),
            "location": images_result["location"],
            "date_range": images_result["date_range"],
            "satellite_data": images_result["images"],
            "ai_analysis": analysis_result,
            "change_detection": "Satellite comparison completed"
        }
        
        # Save result
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_path = f"satellite_analysis_{timestamp}.json"
        with open(output_path, 'w') as f:
            json.dump(result, f, indent=2)
        
        return JSONResponse(content=result)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Satellite analysis failed: {str(e)}")

@app.post("/satellite/predict")
async def predict_future_impact(
    latitude: float = Form(...),
    longitude: float = Form(...),
    start_date: str = Form(...),
    end_date: str = Form(...),
    time_period: str = Form("5_years")
):
    """Predict future environmental impact based on historical satellite data"""
    if satellite_service is None:
        raise HTTPException(status_code=500, detail="Satellite service not available")
    
    try:
        # Get satellite data
        images_result = satellite_service.get_satellite_images(
            latitude, longitude, start_date, end_date
        )
        
        if not images_result["success"]:
            raise HTTPException(status_code=400, detail=images_result["error"])
        
        # Analyze changes
        change_analysis = {
            "change_percentage": 15.5,  # Example value - would be calculated from actual data
            "change_analysis": {
                "change_types": ["deforestation", "urbanization"]
            }
        }
        
        # Predict future impact
        predictions = satellite_service.predict_future_impact(change_analysis, time_period)
        
        result = {
            "timestamp": datetime.now().isoformat(),
            "location": images_result["location"],
            "analysis_period": {"start": start_date, "end": end_date},
            "prediction_period": time_period,
            "current_changes": change_analysis,
            "future_predictions": predictions,
            "recommendations": predictions.get("recommendations", [])
        }
        
        return JSONResponse(content=result)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")

@app.get("/satellite/available-dates")
async def get_available_dates(latitude: float, longitude: float):
    """Get available satellite image dates for a location"""
    if satellite_service is None:
        raise HTTPException(status_code=500, detail="Satellite service not available")
    
    try:
        # This would typically query satellite databases
        # For demo, return sample dates
        available_dates = {
            "location": {"lat": latitude, "lon": longitude},
            "available_dates": [
                "2020-01-15", "2020-06-20", "2021-01-10", "2021-06-15",
                "2022-01-05", "2022-06-10", "2023-01-01", "2023-06-05",
                "2024-01-01", "2024-06-01"
            ],
            "satellite_sources": ["Landsat 8", "Sentinel-2", "NASA EPIC"]
        }
        
        return JSONResponse(content=available_dates)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get dates: {str(e)}")

async def analyze_satellite_changes(satellite_data: Dict) -> Dict:
    """Analyze satellite images using AI model"""
    try:
        if model is None:
            return {"error": "AI model not available"}
        
        # This would process satellite images through the AI model
        # For now, return sample analysis
        return {
            "glaciers_detected": True,
            "drainage_changes": True,
            "road_development": False,
            "environmental_impact": "MODERATE",
            "confidence_score": 0.85
        }
        
    except Exception as e:
        return {"error": f"AI analysis failed: {str(e)}"}

# ===== SOS Emergency Endpoints =====

@app.post("/sos/send-alert")
async def send_sos_alert(
    latitude: float = Form(...),
    longitude: float = Form(...),
    message: str = Form(""),
    alert_type: str = Form("general")
):
    """Send SOS emergency alert"""
    if sos_service is None:
        raise HTTPException(status_code=500, detail="SOS service not available")
    
    try:
        user_location = {
            "latitude": latitude,
            "longitude": longitude
        }
        
        result = sos_service.send_sos_alert(user_location, message, alert_type)
        
        return JSONResponse(content=result)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"SOS alert failed: {str(e)}")

@app.get("/sos/contacts")
async def get_emergency_contacts():
    """Get all emergency contacts"""
    if sos_service is None:
        raise HTTPException(status_code=500, detail="SOS service not available")
    
    try:
        contacts = sos_service.get_emergency_contacts()
        return JSONResponse(content={"contacts": contacts})
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get contacts: {str(e)}")

@app.post("/sos/add-contact")
async def add_emergency_contact(
    name: str = Form(...),
    phone: str = Form(...),
    relationship: str = Form("Emergency Contact")
):
    """Add a new emergency contact"""
    if sos_service is None:
        raise HTTPException(status_code=500, detail="SOS service not available")
    
    try:
        contact = sos_service.add_emergency_contact(name, phone, relationship)
        return JSONResponse(content={"success": True, "contact": contact})
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to add contact: {str(e)}")

@app.delete("/sos/remove-contact/{contact_id}")
async def remove_emergency_contact(contact_id: int):
    """Remove an emergency contact"""
    if sos_service is None:
        raise HTTPException(status_code=500, detail="SOS service not available")
    
    try:
        sos_service.remove_emergency_contact(contact_id)
        return JSONResponse(content={"success": True, "message": "Contact removed"})
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to remove contact: {str(e)}")

@app.get("/sos/history")
async def get_sos_history(limit: int = 10):
    """Get SOS alert history"""
    if sos_service is None:
        raise HTTPException(status_code=500, detail="SOS service not available")
    
    try:
        history = sos_service.get_alert_history(limit)
        return JSONResponse(content={"alerts": history})
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get history: {str(e)}")

@app.get("/sos/emergency-services")
async def get_emergency_services():
    """Get emergency service numbers"""
    if sos_service is None:
        raise HTTPException(status_code=500, detail="SOS service not available")
    
    try:
        services = sos_service.get_emergency_services()
        return JSONResponse(content={"services": services})
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get services: {str(e)}")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
