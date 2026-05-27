import os
import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D
from tensorflow.keras.models import Model

# Parameters
dataset_dir = 'currency_data/dataset'
img_size = (224, 224)
batch_size = 32
epochs = 2 # Train just 2 epochs for demonstration due to time/compute constraints

def train_and_export():
    if not os.path.exists(dataset_dir):
        print("Dataset not found!")
        return

    # Data Generators
    datagen = ImageDataGenerator(validation_split=0.2, rescale=1./255)

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

    # Save class indices
    class_indices = train_generator.class_indices
    labels = {v: k for k, v in class_indices.items()}
    with open('currency_labels.txt', 'w') as f:
        for i in range(len(labels)):
            f.write(f"{labels[i]}\n")
    print(f"Saved {len(labels)} classes to currency_labels.txt")

    # Build Model
    base_model = MobileNetV2(weights='imagenet', include_top=False, input_shape=(224, 224, 3))
    base_model.trainable = False # Freeze base model

    x = base_model.output
    x = GlobalAveragePooling2D()(x)
    x = Dense(128, activation='relu')(x)
    predictions = Dense(len(labels), activation='softmax')(x)

    model = Model(inputs=base_model.input, outputs=predictions)

    model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

    # Train
    print("Starting training...")
    model.fit(
        train_generator,
        validation_data=val_generator,
        epochs=epochs
    )

    # Export to TFLite
    print("Exporting to TFLite...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    # Enable optimizations
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()

    with open('currency.tflite', 'wb') as f:
        f.write(tflite_model)
    print("Model saved to currency.tflite")

if __name__ == '__main__':
    train_and_export()
