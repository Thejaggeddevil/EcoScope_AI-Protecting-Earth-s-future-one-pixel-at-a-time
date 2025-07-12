import torch
import torchvision.transforms as transforms
from PIL import Image
import matplotlib.pyplot as plt
import os

# UNet model import
from models.unet import UNet

# ✅ Device configuration
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# ✅ Load model
model = UNet(in_channels=6, out_channels=1).to(device)
model.load_state_dict(torch.load("models/unet_model.pth", map_location=device))
model.eval()

# ✅ Image paths
before_path = "data/before.png"
after_path = "data/after.png"

# ✅ Image transform
transform = transforms.Compose([
    transforms.Resize((256, 256)),
    transforms.ToTensor()
])

# ✅ Load and transform images
before_img = transform(Image.open(before_path).convert("RGB"))
after_img = transform(Image.open(after_path).convert("RGB"))

# ✅ Concatenate images along channel dimension
input_tensor = torch.cat((before_img, after_img), dim=0).unsqueeze(0).to(device)

# ✅ Predict
with torch.no_grad():
    output = model(input_tensor)
    output_image = output.squeeze().cpu().numpy()

# ✅ Show result
plt.imshow(output_image, cmap='gray')
plt.title("Change Map")
plt.axis('off')
plt.show()

# ✅ Save result
os.makedirs("results", exist_ok=True)
plt.imsave("results/change_map.png", output_image, cmap='gray')
print("✅ Saved: results/change_map.png")
