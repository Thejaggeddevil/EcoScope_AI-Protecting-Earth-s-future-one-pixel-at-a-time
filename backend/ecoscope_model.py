import torch
import cv2
import numpy as np
import os
import json
from datetime import datetime
from typing import Dict, List, Tuple, Optional
import base64
from io import BytesIO
from PIL import Image

class EcoScopeModel:
    """EcoScope AI Model for Environmental Analysis"""
    
    def __init__(self, model_path: str = "checkpoints/best_model.pth"):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.model = self._load_model(model_path)
        self.image_size = 256  # Standard size for analysis
        
    def _load_model(self, model_path: str):
        """Load the trained model"""
        try:
            # Import the model architecture
            import sys
            sys.path.append(os.path.dirname(os.path.dirname(__file__)))
            from train_fast import OptimizedUNet
            
            model = OptimizedUNet().to(self.device)
            
            if os.path.exists(model_path):
                checkpoint = torch.load(model_path, map_location=self.device)
                model.load_state_dict(checkpoint['model_state_dict'])
                print(f"✅ EcoScope Model loaded successfully!")
                return model
            else:
                print(f"⚠️ Model not found at {model_path}, using untrained model")
                return model
        except Exception as e:
            print(f"❌ Error loading model: {e}")
            return None
    
    def preprocess_image(self, image_data: bytes) -> Optional[torch.Tensor]:
        """Preprocess image for model input"""
        try:
            # Convert bytes to image
            image = Image.open(BytesIO(image_data))
            image = np.array(image)
            
            # Convert to RGB if needed
            if len(image.shape) == 3 and image.shape[2] == 4:
                image = cv2.cvtColor(image, cv2.COLOR_RGBA2RGB)
            elif len(image.shape) == 3 and image.shape[2] == 3:
                image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            
            # Resize image
            image = cv2.resize(image, (self.image_size, self.image_size))
            
            # Normalize and convert to tensor
            image = image.astype(np.float32) / 255.0
            image = torch.tensor(image).permute(2, 0, 1).unsqueeze(0)
            
            return image.to(self.device)
        except Exception as e:
            print(f"❌ Error preprocessing image: {e}")
            return None
    
    def analyze_environmental_changes(self, image_data: bytes) -> Dict:
        """Analyze environmental changes in the image"""
        if self.model is None:
            return {"error": "Model not loaded"}
        
        # Preprocess image
        input_tensor = self.preprocess_image(image_data)
        if input_tensor is None:
            return {"error": "Failed to preprocess image"}
        
        # Get prediction
        self.model.eval()
        with torch.no_grad():
            prediction = self.model(input_tensor)
        
        # Convert prediction to numpy
        pred_np = prediction.squeeze().cpu().numpy()
        
        # Analyze different environmental features
        analysis = self._analyze_features(pred_np)
        
        return analysis
    
    def _analyze_features(self, prediction: np.ndarray) -> Dict:
        """Analyze specific environmental features"""
        # Threshold for detection
        threshold = 0.5
        
        # Calculate metrics
        affected_area = np.sum(prediction > threshold) / prediction.size * 100
        impact_intensity = np.mean(prediction) * 100
        
        # Detect specific features
        features = {
            "glaciers": self._detect_glaciers(prediction),
            "drainage_systems": self._detect_drainage_systems(prediction),
            "road_networks": self._detect_road_networks(prediction),
            "environmental_changes": self._detect_environmental_changes(prediction)
        }
        
        # Generate impact assessment
        impact_level = self._assess_impact_level(affected_area, impact_intensity)
        
        return {
            "timestamp": datetime.now().isoformat(),
            "affected_area_percentage": round(affected_area, 2),
            "impact_intensity_percentage": round(impact_intensity, 2),
            "impact_level": impact_level,
            "features_detected": features,
            "recommendations": self._generate_recommendations(features, impact_level)
        }
    
    def _detect_glaciers(self, prediction: np.ndarray) -> Dict:
        """Detect glacier-related changes"""
        # Look for large, connected areas (glacier characteristics)
        from scipy import ndimage
        
        # Find connected components
        labeled, num_features = ndimage.label(prediction > 0.6)
        
        glacier_areas = []
        for i in range(1, num_features + 1):
            area = np.sum(labeled == i)
            if area > 100:  # Minimum area threshold
                glacier_areas.append(area)
        
        return {
            "detected": len(glacier_areas) > 0,
            "count": len(glacier_areas),
            "total_area": sum(glacier_areas),
            "melting_indicators": np.mean(prediction) > 0.7
        }
    
    def _detect_drainage_systems(self, prediction: np.ndarray) -> Dict:
        """Detect urban drainage systems"""
        # Look for linear patterns (drainage channels)
        from scipy import ndimage
        
        # Apply morphological operations to detect linear structures
        kernel = np.array([[1, 1, 1], [1, 1, 1], [1, 1, 1]])
        dilated = ndimage.binary_dilation(prediction > 0.5, structure=kernel)
        
        # Count linear features
        linear_features = np.sum(dilated) - np.sum(prediction > 0.5)
        
        return {
            "detected": linear_features > 50,
            "drainage_density": linear_features / prediction.size * 100,
            "network_complexity": "high" if linear_features > 100 else "medium" if linear_features > 50 else "low"
        }
    
    def _detect_road_networks(self, prediction: np.ndarray) -> Dict:
        """Detect road networks"""
        # Look for straight, parallel lines (road characteristics)
        from scipy import ndimage
        
        # Apply edge detection
        edges = cv2.Canny((prediction * 255).astype(np.uint8), 50, 150)
        
        # Count road-like features
        road_pixels = np.sum(edges > 0)
        
        return {
            "detected": road_pixels > 100,
            "road_density": road_pixels / prediction.size * 100,
            "network_type": "dense" if road_pixels > 200 else "sparse"
        }
    
    def _detect_environmental_changes(self, prediction: np.ndarray) -> Dict:
        """Detect general environmental changes"""
        # Analyze overall patterns
        changes = {
            "deforestation": np.mean(prediction[0:prediction.shape[0]//2, :]) > 0.6,
            "urbanization": np.mean(prediction[prediction.shape[0]//2:, :]) > 0.5,
            "water_bodies": np.sum(prediction < 0.2) / prediction.size * 100,
            "vegetation_loss": np.mean(prediction) > 0.6
        }
        
        return changes
    
    def _assess_impact_level(self, affected_area: float, impact_intensity: float) -> str:
        """Assess the overall impact level"""
        if affected_area > 50 or impact_intensity > 70:
            return "CRITICAL"
        elif affected_area > 30 or impact_intensity > 50:
            return "HIGH"
        elif affected_area > 15 or impact_intensity > 30:
            return "MODERATE"
        else:
            return "LOW"
    
    def _generate_recommendations(self, features: Dict, impact_level: str) -> List[str]:
        """Generate recommendations based on analysis"""
        recommendations = []
        
        if impact_level in ["CRITICAL", "HIGH"]:
            recommendations.append("Immediate environmental assessment required")
            recommendations.append("Consider implementing conservation measures")
        
        if features["glaciers"]["detected"]:
            recommendations.append("Monitor glacier melting patterns")
            recommendations.append("Assess impact on water resources")
        
        if features["drainage_systems"]["detected"]:
            recommendations.append("Review urban drainage infrastructure")
            recommendations.append("Check for flooding risks")
        
        if features["road_networks"]["detected"]:
            recommendations.append("Assess infrastructure development impact")
            recommendations.append("Monitor habitat fragmentation")
        
        return recommendations
    
    def save_analysis_result(self, analysis: Dict, output_path: str = "analysis_results.json"):
        """Save analysis results to file"""
        try:
            with open(output_path, 'w') as f:
                json.dump(analysis, f, indent=2)
            print(f"✅ Analysis saved to {output_path}")
        except Exception as e:
            print(f"❌ Error saving analysis: {e}")

# Example usage
if __name__ == "__main__":
    model = EcoScopeModel()
    print("🌿 EcoScope AI Model ready for environmental analysis!") 