import torch
import cv2
import numpy as np
import matplotlib.pyplot as plt
from train_unet import UNet
import os
import json

# Load model
model = UNet()
model.load_state_dict(torch.load("lake_unet.pt", map_location="cpu"))
model.eval()

def preprocess_image(image_path):
    image = cv2.imread(image_path)
    image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    image_tensor = torch.tensor(image).permute(2, 0, 1).unsqueeze(0).float() / 255.0
    return image, image_tensor

def calculate_area_change(binary_mask, resolution_m=10):
    # Total pixels marked as change
    changed_pixels = np.sum(binary_mask == 255)
    pixel_area_km2 = (resolution_m ** 2) / 1e6
    return round(changed_pixels * pixel_area_km2, 4)  # in km²

def classify_change_type(area_km2):
    if area_km2 > 2:
        return "Glacial Lake Expansion", "Risk of flooding", ["Evacuate nearby zones", "Monitor water levels"]
    elif area_km2 > 0.5:
        return "Urban Drainage Shift", "Urban flood risk", ["Unblock drainage", "Increase monitoring"]
    else:
        return "Road Network Extension", "Construction impact", ["Review construction permits", "Check water flow"]

def predict_single_image(image_path, lat=27.0120, lon=88.1230, save_path=None):
    orig_img, input_tensor = preprocess_image(image_path)

    with torch.no_grad():
        pred = model(input_tensor)
        pred_mask = pred.squeeze().numpy()

    binary_mask = (pred_mask > 0.5).astype(np.uint8) * 255
    area_km2 = calculate_area_change(binary_mask)
    change_type, impact, measures = classify_change_type(area_km2)

    overlay = orig_img.copy()
    overlay[binary_mask == 255] = [255, 0, 0]

    # Show result
    plt.figure(figsize=(15,5))
    plt.subplot(1, 3, 1)
    plt.title("Original Image")
    plt.imshow(orig_img)
    plt.axis('off')

    plt.subplot(1, 3, 2)
    plt.title("Predicted Mask")
    plt.imshow(pred_mask, cmap='gray')
    plt.axis('off')

    plt.subplot(1, 3, 3)
    plt.title("Overlay")
    plt.imshow(overlay)
    plt.axis('off')

    plt.tight_layout()
    plt.show()

    if save_path:
        cv2.imwrite(save_path, cv2.cvtColor(overlay, cv2.COLOR_RGB2BGR))
        print(f"Overlay saved to {save_path}")

    # Build full result
    result = {
        "location": {"lat": lat, "lon": lon},
        "change_detected": True,
        "change_type": change_type,
        "area_changed_km2": area_km2,
        "suspected_reason": f"Likely {change_type.lower()} in region",
        "impact": impact,
        "suggested_measures": measures
    }

    print("\n==== JSON Output ====")
    print(json.dumps(result, indent=4))

    return result

# Example usage
if __name__ == "__main__":
    test_image_path = "data/val/images/sample1.png"
    predict_single_image(test_image_path)
