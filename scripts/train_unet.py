import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from torchvision import transforms
import os
from dataset import ChangeDataset
from unet import UNet  # We'll create this next
import matplotlib.pyplot as plt

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# Paths
train_img_dir = "data/train/images"
train_mask_dir = "data/train/masks"
val_img_dir = "data/val/images"
val_mask_dir = "data/val/masks"

# Hyperparameters
num_classes = 5  # 0: No change, 1: Glacier, 2: Road, 3: Drainage, 4: Urban
batch_size = 4
lr = 0.001
epochs = 15

# Transforms
transform = transforms.Compose([
    transforms.Resize((256, 256)),
    transforms.ToTensor()
])

# Datasets
train_dataset = ChangeDataset(train_img_dir, train_mask_dir, transform)
val_dataset = ChangeDataset(val_img_dir, val_mask_dir, transform)
train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
val_loader = DataLoader(val_dataset, batch_size=1)

# Model
model = UNet(n_channels=3, n_classes=num_classes).to(device)
criterion = nn.CrossEntropyLoss()
optimizer = torch.optim.Adam(model.parameters(), lr=lr)

# Training Loop
for epoch in range(epochs):
    model.train()
    total_loss = 0
    for imgs, masks in train_loader:
        imgs, masks = imgs.to(device), masks.to(device)
        outputs = model(imgs)
        loss = criterion(outputs, masks)
        optimizer.zero_grad()
        loss.backward()
        optimizer.step()
        total_loss += loss.item()

    print(f"Epoch {epoch+1}/{epochs} | Loss: {total_loss:.4f}")

# Save model
os.makedirs("models", exist_ok=True)
torch.save(model.state_dict(), "models/lake_unet.pt")
print("✅ Model trained and saved.")
