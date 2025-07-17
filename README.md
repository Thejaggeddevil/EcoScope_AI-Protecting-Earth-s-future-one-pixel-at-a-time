# 🌍 EcoScope AI
**Protecting Earth’s Future, One Pixel at a Time**

EcoScope AI is a powerful AI-powered satellite image analysis platform that detects critical environmental changes—such as glacial lake expansion, illegal urbanization, road network growth, and drainage failures. Built for resilience, EcoScope empowers governments, NGOs, and researchers to respond to ecological shifts faster and smarter.

---

## 🚀 Features

- 🔍 **Change Detection**: Detect changes between two satellite images using deep learning (U-Net-based model).
- 🧠 **AI-Powered Analysis**: Automatically highlight regions of change with segmentation masks.
- 📐 **Area Calculation**: Estimate the area (in sq. km) of detected changes.
- ⚠️ **Risk Classification**: Predict the potential impact (e.g., flooding, urban stress).
- 📈 **Dashboard Ready**: Outputs designed for integration into dashboards or GIS tools.

---

## 🧠 AI Model

- **Architecture**: Pretrained [U-Net](https://arxiv.org/abs/1505.04597) segmentation model
- **Input**: Pair of satellite images (before & after)
- **Output**: Binary mask + change type + risk level

Trained on annotated satellite datasets (roads, glaciers, urban drainage systems) and fine-tuned for imbalanced classes using focal loss.

---
Tech Stack

| Layer         | Tools/Tech                     |
| ------------- | ------------------------------ |
| AI/ML Model   | PyTorch, U-Net, Focal Loss     |
| Data Handling | OpenCV, NumPy, Pandas          |
| Frontend      | Jetpack Compose / Flutter      |
| Backend       | Flask / FastAPI (optional)     |
| Hosting       | GitHub, Streamlit, HuggingFace |

---
Presentation
ppt (https://drive.google.com/file/d/1kwSc3-zMp8GVF0r0gMhg_I4-vo5Iofj9/view?usp=sharing)
