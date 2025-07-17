import requests
import json
import os
from datetime import datetime, timedelta
import numpy as np
import cv2
from typing import Dict, List, Tuple, Optional
import rasterio
from rasterio.warp import calculate_default_transform, reproject, Resampling
import matplotlib.pyplot as plt
from io import BytesIO
import base64

class SatelliteService:
    """Service to fetch and compare satellite images"""
    
    def __init__(self):
        self.nasa_api_key = os.getenv("NASA_API_KEY", "DEMO_KEY")  # Get from environment
        self.landsat_api_key = os.getenv("LANDSAT_API_KEY", "")  # For Landsat data
        self.sentinel_api_key = os.getenv("SENTINEL_API_KEY", "")  # For Sentinel data
        
    def get_satellite_images(self, latitude: float, longitude: float, 
                           start_date: str, end_date: str) -> Dict:
        """Fetch satellite images for given location and date range"""
        try:
            # Try multiple satellite sources
            images = {}
            
            # 1. NASA EPIC (Earth Polychromatic Imaging Camera)
            epic_images = self._fetch_nasa_epic(latitude, longitude, start_date, end_date)
            if epic_images:
                images['nasa_epic'] = epic_images
            
            # 2. Landsat 8/9 data
            landsat_images = self._fetch_landsat(latitude, longitude, start_date, end_date)
            if landsat_images:
                images['landsat'] = landsat_images
            
            # 3. Sentinel-2 data
            sentinel_images = self._fetch_sentinel(latitude, longitude, start_date, end_date)
            if sentinel_images:
                images['sentinel'] = sentinel_images
            
            return {
                "success": True,
                "images": images,
                "location": {"lat": latitude, "lon": longitude},
                "date_range": {"start": start_date, "end": end_date}
            }
            
        except Exception as e:
            return {
                "success": False,
                "error": str(e),
                "images": {}
            }
    
    def _fetch_nasa_epic(self, lat: float, lon: float, start_date: str, end_date: str) -> List[Dict]:
        """Fetch NASA EPIC images"""
        try:
            # NASA EPIC API endpoint
            url = f"https://api.nasa.gov/EPIC/api/natural/date/{start_date}"
            params = {
                "api_key": self.nasa_api_key
            }
            
            response = requests.get(url, params=params)
            if response.status_code == 200:
                data = response.json()
                return data[:5]  # Return first 5 images
            return []
            
        except Exception as e:
            print(f"Error fetching NASA EPIC: {e}")
            return []
    
    def _fetch_landsat(self, lat: float, lon: float, start_date: str, end_date: str) -> List[Dict]:
        """Fetch Landsat images"""
        try:
            # USGS Earth Explorer API for Landsat
            url = "https://earthexplorer.usgs.gov/inventory/json/v/1.4.0/metadata"
            params = {
                "datasetName": "LANDSAT_8_C1",
                "spatialFilter": {
                    "filterType": "mbr",
                    "lowerLeft": {"latitude": lat - 0.1, "longitude": lon - 0.1},
                    "upperRight": {"latitude": lat + 0.1, "longitude": lon + 0.1}
                },
                "temporalFilter": {
                    "startDate": start_date,
                    "endDate": end_date
                },
                "api_key": self.landsat_api_key
            }
            
            response = requests.get(url, params=params)
            if response.status_code == 200:
                data = response.json()
                return data.get("results", [])[:5]
            return []
            
        except Exception as e:
            print(f"Error fetching Landsat: {e}")
            return []
    
    def _fetch_sentinel(self, lat: float, lon: float, start_date: str, end_date: str) -> List[Dict]:
        """Fetch Sentinel-2 images"""
        try:
            # Copernicus Open Access Hub API
            url = "https://scihub.copernicus.eu/dhus/search"
            params = {
                "q": f"platformname:Sentinel-2 AND footprint:\"Intersects(POINT({lon} {lat}))\" AND beginPosition:[{start_date}T00:00:00.000Z TO {end_date}T23:59:59.999Z]",
                "format": "json",
                "rows": 5
            }
            
            response = requests.get(url, params=params)
            if response.status_code == 200:
                data = response.json()
                return data.get("feed", {}).get("entry", [])
            return []
            
        except Exception as e:
            print(f"Error fetching Sentinel: {e}")
            return []
    
    def compare_images(self, image1_path: str, image2_path: str) -> Dict:
        """Compare two satellite images and detect changes"""
        try:
            # Load images
            img1 = cv2.imread(image1_path)
            img2 = cv2.imread(image2_path)
            
            if img1 is None or img2 is None:
                return {"error": "Could not load images"}
            
            # Resize images to same size
            height, width = min(img1.shape[:2], img2.shape[:2])
            img1 = cv2.resize(img1, (width, height))
            img2 = cv2.resize(img2, (width, height))
            
            # Convert to grayscale for comparison
            gray1 = cv2.cvtColor(img1, cv2.COLOR_BGR2GRAY)
            gray2 = cv2.cvtColor(img2, cv2.COLOR_BGR2GRAY)
            
            # Calculate difference
            diff = cv2.absdiff(gray1, gray2)
            
            # Apply threshold to highlight significant changes
            _, thresh = cv2.threshold(diff, 30, 255, cv2.THRESH_BINARY)
            
            # Find contours of changed areas
            contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
            
            # Calculate change metrics
            total_pixels = width * height
            changed_pixels = np.sum(thresh > 0)
            change_percentage = (changed_pixels / total_pixels) * 100
            
            # Analyze change types
            change_analysis = self._analyze_changes(img1, img2, thresh)
            
            # Generate visualization
            comparison_image = self._create_comparison_visualization(img1, img2, thresh)
            
            return {
                "success": True,
                "change_percentage": round(change_percentage, 2),
                "changed_pixels": int(changed_pixels),
                "total_pixels": total_pixels,
                "change_analysis": change_analysis,
                "comparison_image": comparison_image,
                "contours_count": len(contours)
            }
            
        except Exception as e:
            return {"error": f"Comparison failed: {str(e)}"}
    
    def _analyze_changes(self, img1: np.ndarray, img2: np.ndarray, diff_mask: np.ndarray) -> Dict:
        """Analyze types of changes in the images"""
        try:
            # Convert to different color spaces for analysis
            hsv1 = cv2.cvtColor(img1, cv2.COLOR_BGR2HSV)
            hsv2 = cv2.cvtColor(img2, cv2.COLOR_BGR2HSV)
            
            # Analyze vegetation changes (green channel)
            green_diff = cv2.absdiff(hsv1[:, :, 1], hsv2[:, :, 1])
            vegetation_change = np.mean(green_diff[diff_mask > 0])
            
            # Analyze water changes (blue channel)
            blue_diff = cv2.absdiff(img1[:, :, 0], img2[:, :, 0])
            water_change = np.mean(blue_diff[diff_mask > 0])
            
            # Analyze urban changes (brightness)
            brightness_diff = cv2.absdiff(cv2.cvtColor(img1, cv2.COLOR_BGR2GRAY), 
                                        cv2.cvtColor(img2, cv2.COLOR_BGR2GRAY))
            urban_change = np.mean(brightness_diff[diff_mask > 0])
            
            return {
                "vegetation_change": round(vegetation_change, 2),
                "water_change": round(water_change, 2),
                "urban_change": round(urban_change, 2),
                "change_types": self._classify_changes(vegetation_change, water_change, urban_change)
            }
            
        except Exception as e:
            return {"error": f"Change analysis failed: {str(e)}"}
    
    def _classify_changes(self, vegetation: float, water: float, urban: float) -> List[str]:
        """Classify the types of changes detected"""
        changes = []
        
        if vegetation > 50:
            changes.append("deforestation")
        elif vegetation < -30:
            changes.append("vegetation_growth")
            
        if water > 40:
            changes.append("water_body_reduction")
        elif water < -40:
            changes.append("water_body_expansion")
            
        if urban > 60:
            changes.append("urbanization")
        elif urban < -50:
            changes.append("urban_degradation")
            
        if not changes:
            changes.append("minor_environmental_changes")
            
        return changes
    
    def _create_comparison_visualization(self, img1: np.ndarray, img2: np.ndarray, diff_mask: np.ndarray) -> str:
        """Create a visualization of the comparison"""
        try:
            # Create comparison image
            fig, axes = plt.subplots(1, 3, figsize=(15, 5))
            
            # Original image 1
            axes[0].imshow(cv2.cvtColor(img1, cv2.COLOR_BGR2RGB))
            axes[0].set_title("Before")
            axes[0].axis('off')
            
            # Original image 2
            axes[1].imshow(cv2.cvtColor(img2, cv2.COLOR_BGR2RGB))
            axes[1].set_title("After")
            axes[1].axis('off')
            
            # Difference mask
            axes[2].imshow(diff_mask, cmap='hot')
            axes[2].set_title("Changes Detected")
            axes[2].axis('off')
            
            plt.tight_layout()
            
            # Save to bytes
            buffer = BytesIO()
            plt.savefig(buffer, format='png', dpi=150, bbox_inches='tight')
            buffer.seek(0)
            
            # Convert to base64
            image_base64 = base64.b64encode(buffer.getvalue()).decode()
            plt.close()
            
            return f"data:image/png;base64,{image_base64}"
            
        except Exception as e:
            print(f"Error creating visualization: {e}")
            return ""
    
    def predict_future_impact(self, change_analysis: Dict, time_period: str = "5_years") -> Dict:
        """Predict future environmental impact based on current changes"""
        try:
            change_percentage = change_analysis.get("change_percentage", 0)
            change_types = change_analysis.get("change_analysis", {}).get("change_types", [])
            
            predictions = {
                "time_period": time_period,
                "risk_level": "LOW",
                "predictions": [],
                "recommendations": []
            }
            
            # Risk assessment
            if change_percentage > 20:
                predictions["risk_level"] = "HIGH"
            elif change_percentage > 10:
                predictions["risk_level"] = "MODERATE"
            
            # Generate predictions based on change types
            for change_type in change_types:
                if change_type == "deforestation":
                    predictions["predictions"].append("Continued deforestation may lead to soil erosion and biodiversity loss")
                    predictions["recommendations"].append("Implement reforestation programs and stricter logging regulations")
                    
                elif change_type == "urbanization":
                    predictions["predictions"].append("Urban expansion may increase pollution and reduce green spaces")
                    predictions["recommendations"].append("Plan sustainable urban development with green infrastructure")
                    
                elif change_type == "water_body_reduction":
                    predictions["predictions"].append("Water body reduction may affect local ecosystems and water availability")
                    predictions["recommendations"].append("Monitor water usage and implement conservation measures")
                    
                elif change_type == "vegetation_growth":
                    predictions["predictions"].append("Vegetation growth indicates positive environmental recovery")
                    predictions["recommendations"].append("Continue current conservation efforts")
            
            return predictions
            
        except Exception as e:
            return {"error": f"Prediction failed: {str(e)}"}

# Example usage
if __name__ == "__main__":
    service = SatelliteService()
    print("🌍 Satellite Service initialized!") 