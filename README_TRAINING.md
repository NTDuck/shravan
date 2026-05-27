# Vietnamese Currency Model Training

This folder contains the tools to train a custom YOLOv5 model for Vietnamese currency detection using the provided dataset.

## Prerequisites
- Python 3.8+
- PyTorch
- Ultralytics YOLOv5 (`pip install -r https://raw.githubusercontent.com/ultralytics/yolov5/master/requirements.txt`)

## Dataset Structure
The dataset is expected in `currency_data/dataset/` with subfolders for each denomination (001000, 002000, etc.).

## Steps to Train
1. **Prepare Data**: Run the preparation script to convert the classification folder structure into YOLOv5 detection format (with center-weighted dummy labels).
   ```bash
   python currency_trainer.py
   ```
2. **Train Model**: Run the YOLOv5 training script.
   ```bash
   python train.py --img 640 --batch 16 --epochs 100 --data yolo_data/data.yaml --weights yolov5s.pt
   ```
3. **Export to TFLite**: Convert the best-trained model to TFLite format for Android.
   ```bash
   python export.py --weights runs/train/exp/weights/best.pt --include tflite --int8
   ```

## Integration
Once you have `best-fp16.tflite`, rename it to `currency.tflite` and place it in the `app/src/main/assets/` directory of the Android project.
The app is already configured to load this model when the Currency screen is active.
