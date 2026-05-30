from inference_sdk import InferenceHTTPClient
import sys

# User-provided code for Roboflow Inference
def run_inference(image_path):
    CLIENT = InferenceHTTPClient(
        api_url="https://serverless.roboflow.com",
        api_key="cCunQhOIWzChQHaRKPia"
    )

    result = CLIENT.infer(image_path, model_id="vietnamese-currency-lgi9i/5")
    return result

if __name__ == "__main__":
    if len(sys.argv) > 1:
        image_path = sys.argv[1]
        print(run_inference(image_path))
    else:
        print("Usage: python currency_inference.py <path_to_image>")
