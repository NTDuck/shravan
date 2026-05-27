import os
import zipfile
import shutil
import random

# This script prepares the dataset and provides the training command for YOLOv5
# Since the dataset is in folders, we'll treat it as a classification task or 
# convert it to object detection by assuming the currency occupies the center.

def prepare_data():
    dataset_path = "currency_data/dataset"
    if not os.path.exists(dataset_path):
        print("Dataset not found at currency_data/dataset")
        return

    classes = [d for d in os.listdir(dataset_path) if os.path.isdir(os.path.join(dataset_path, d))]
    print(f"Found classes: {classes}")

    # Create YOLOv5 directory structure
    os.makedirs("yolo_data/images/train", exist_ok=True)
    os.makedirs("yolo_data/images/val", exist_ok=True)
    os.makedirs("yolo_data/labels/train", exist_ok=True)
    os.makedirs("yolo_data/labels/val", exist_ok=True)

    for i, cls in enumerate(classes):
        cls_path = os.path.join(dataset_path, cls)
        images = [f for f in os.listdir(cls_path) if f.endswith(('.png', '.jpg', '.jpeg'))]
        random.shuffle(images)
        
        split = int(0.8 * len(images))
        train_imgs = images[:split]
        val_imgs = images[split:]

        for img_name in train_imgs:
            shutil.copy(os.path.join(cls_path, img_name), f"yolo_data/images/train/{cls}_{img_name}")
            # Create a dummy label (center of image)
            with open(f"yolo_data/labels/train/{cls}_{img_name.rsplit('.', 1)[0]}.txt", "w") as f:
                f.write(f"{i} 0.5 0.5 0.8 0.8\n")

        for img_name in val_imgs:
            shutil.copy(os.path.join(cls_path, img_name), f"yolo_data/images/val/{cls}_{img_name}")
            with open(f"yolo_data/labels/val/{cls}_{img_name.rsplit('.', 1)[0]}.txt", "w") as f:
                f.write(f"{i} 0.5 0.5 0.8 0.8\n")

    # Create data.yaml
    with open("yolo_data/data.yaml", "w") as f:
        f.write(f"train: ../yolo_data/images/train\n")
        f.write(f"val: ../yolo_data/images/val\n\n")
        f.write(f"nc: {len(classes)}\n")
        f.write(f"names: {classes}\n")

if __name__ == "__main__":
    prepare_data()
    print("\nData prepared in yolo_data folder.")
    print("To train the model, run:")
    print("python train.py --img 640 --batch 16 --epochs 100 --data yolo_data/data.yaml --weights yolov5s.pt")
    print("\nAfter training, export to TFLite:")
    print("python export.py --weights runs/train/exp/weights/best.pt --include tflite --int8")
