import os
import cv2
import numpy as np
from tqdm import tqdm

# Path structure
base_dir = "data"
sets = ["train", "val"]
image_size = (256, 256)
num_images = 10  # change this to generate more

# Function to generate dummy image and mask
def create_dummy_data(path_images, path_masks):
    os.makedirs(path_images, exist_ok=True)
    os.makedirs(path_masks, exist_ok=True)
    for i in tqdm(range(num_images), desc=f"Creating data in {path_images}"):
        img = np.random.randint(0, 256, (*image_size, 3), dtype=np.uint8)
        mask = np.zeros(image_size, dtype=np.uint8)

        # Random rectangle for change
        if np.random.rand() > 0.5:
            x1, y1 = np.random.randint(0, 128, size=2)
            x2, y2 = x1 + np.random.randint(10, 128), y1 + np.random.randint(10, 128)
            cv2.rectangle(mask, (x1, y1), (x2, y2), 1, -1)

        # Save
        cv2.imwrite(os.path.join(path_images, f"image_{i}.png"), img)
        cv2.imwrite(os.path.join(path_masks, f"image_{i}.png"), mask * 255)

# Generate train and val data
for s in sets:
    img_path = os.path.join(base_dir, s, "images")
    mask_path = os.path.join(base_dir, s, "masks")
    create_dummy_data(img_path, mask_path)

print("✅ Dummy data generation completed!")
