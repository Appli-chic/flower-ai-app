import io
import torch
import torchvision.transforms as transforms

from PIL import Image
from flask import Flask, jsonify, request

from le_net import LeNet

app = Flask(__name__)

classes = {
    0: "marguerite",
    1: "pissenlit",
    2: "rose",
    3: "tournesol",
    4: "tulipe",
}

device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')

model = LeNet(5)
flower_ai = torch.load('./flower_ai.pth')
model.load_state_dict(flower_ai)
model.to(device)
model.eval()


def transform_image(image_bytes):
    my_transforms = transforms.Compose([
        transforms.Resize((192, 192)),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
    ])

    image = Image.open(io.BytesIO(image_bytes))
    return my_transforms(image).unsqueeze(0)


def get_prediction(image_bytes):
    tensor = transform_image(image_bytes=image_bytes).to(device)
    outputs = model.forward(tensor)
    _, predicted = torch.max(outputs.data, 1)
    return predicted


@app.route('/')
def hello_world():
    return 'Hello World!'


@app.route('/predict', methods=['POST'])
def predict():

    file = request.data
    prediction = get_prediction(image_bytes=file)
    return jsonify({'prediction': classes[prediction.item()]})


if __name__ == '__main__':
    app.run()
