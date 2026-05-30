import os
import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.applications import ResNet50
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D, Dropout
from tensorflow.keras.models import Model
from tensorflow.keras.optimizers import Adam

# Parameters
dataset_dir = 'currency_data/dataset'
img_size = (224, 224)
batch_size = 32
epochs = 25 # Increased for better accuracy

def train_and_export():
    if not os.path.exists(dataset_dir):
        print(f"Dataset not found at {dataset_dir}!")
        return

    # Advanced Data Augmentation
    datagen = ImageDataGenerator(
        validation_split=0.2,
        rescale=1./255,
        rotation_range=20,
        width_shift_range=0.2,
        height_shift_range=0.2,
        shear_range=0.2,
        zoom_range=0.2,
        horizontal_flip=True,
        fill_mode='nearest'
    )

    train_generator = datagen.flow_from_directory(
        dataset_dir,
        target_size=img_size,
        batch_size=batch_size,
        class_mode='categorical',
        subset='training'
    )

    val_generator = datagen.flow_from_directory(
        dataset_dir,
        target_size=img_size,
        batch_size=batch_size,
        class_mode='categorical',
        subset='validation'
    )

    # Save class indices in a clear format
    class_indices = train_generator.class_indices
    labels = {v: k for k, v in class_indices.items()}
    
    # Map raw folder names to display names if possible
    display_names = {
        "000000": "Background",
        "000200": "200 VND",
        "000500": "500 VND",
        "001000": "1,000 VND",
        "002000": "2,000 VND",
        "005000": "5,000 VND",
        "010000": "10,000 VND",
        "020000": "20,000 VND",
        "050000": "50,000 VND",
        "100000": "100,000 VND",
        "200000": "200,000 VND",
        "500000": "500,000 VND"
    }

    with open('currency_labels.txt', 'w') as f:
        for i in range(len(labels)):
            raw_name = labels[i]
            f.write(f"{display_names.get(raw_name, raw_name)}\n")
            
    # Vietnamese labels
    display_names_vi = {
        "000000": "000000",
        "000200": "200 Đồng",
        "000500": "500 Đồng",
        "001000": "1,000 Đồng",
        "002000": "2,000 Đồng",
        "005000": "5,000 Đồng",
        "010000": "10,000 Đồng",
        "020000": "20,000 Đồng",
        "050000": "50,000 Đồng",
        "100000": "100,000 Đồng",
        "200000": "200,000 Đồng",
        "500000": "500,000 Đồng"
    }
    with open('currency_labels_vi.txt', 'w') as f:
        for i in range(len(labels)):
            raw_name = labels[i]
            f.write(f"{display_names_vi.get(raw_name, raw_name)}\n")
            
    print(f"Saved labels for {len(labels)} classes.")

    # Build Model using ResNet50 as requested (from GitHub repo approach)
    base_model = ResNet50(weights='imagenet', include_top=False, input_shape=(224, 224, 3))
    base_model.trainable = False # Freeze base model for initial training

    x = base_model.output
    x = GlobalAveragePooling2D()(x)
    x = Dense(256, activation='relu')(x)
    x = Dropout(0.5)(x)
    predictions = Dense(len(labels), activation='softmax')(x)

    model = Model(inputs=base_model.input, outputs=predictions)

    model.compile(optimizer=Adam(learning_rate=0.001), loss='categorical_crossentropy', metrics=['accuracy'])

    # Train
    print("Starting training phase 1 (Top layers)...")
    model.fit(
        train_generator,
        validation_data=val_generator,
        epochs=10
    )

    # Fine-tuning: Unfreeze some layers
    print("Fine-tuning base model...")
    for layer in base_model.layers[-20:]:
        layer.trainable = True
    
    model.compile(optimizer=Adam(learning_rate=0.0001), loss='categorical_crossentropy', metrics=['accuracy'])
    model.fit(
        train_generator,
        validation_data=val_generator,
        epochs=15
    )

    # Export to TFLite
    print("Exporting to TFLite...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()

    with open('currency.tflite', 'wb') as f:
        f.write(tflite_model)
    print("Model saved to currency.tflite")

if __name__ == '__main__':
    train_and_export()
