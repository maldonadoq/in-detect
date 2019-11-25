from http.server import HTTPServer, BaseHTTPRequestHandler
from io import BytesIO
import numpy as np
import argparse
from base64 import decodestring
import json
import cv2

net = cv2.dnn.readNet('yolov3.weights','yolov3.cfg')
with open('yolov3.txt', 'r') as f:
    classes = [line.strip() for line in f.readlines()]

def outputLayers(net):
    layer_names = net.getLayerNames()
    output_layers = [layer_names[i[0] - 1] for i in net.getUnconnectedOutLayers()]

    return output_layers

def recognize(image):
    blob = cv2.dnn.blobFromImage(image, 0.00392, (416,416), (0,0,0), True, crop=False)
    net.setInput(blob)
    outs = net.forward(outputLayers(net))

    conf_threshold = 0.5
    tdict = {}
    i = 0
    for out in outs:
        for detection in out:
            scores = detection[5:]
            class_id = np.argmax(scores)
            confidence = scores[class_id]
            if confidence > 0.5:
                p = detection[0] - detection[2] / 2
                q = detection[1] - detection[3] / 2

                r = detection[0] + detection[2] / 2
                s = detection[1] + detection[3] / 2
                
                tdict[i] = {
                    "class": classes[class_id],
                    "confidence": "%.4f"%(confidence),
                    "box": "%.4f,%.4f,%.4f,%.4f"%(p, q, r, s)
                }
                i+=1

    return str.encode(json.dumps(tdict))


class YoloServer(BaseHTTPRequestHandler):

    def do_GET(self):
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b"Hello World\n")

    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        body = self.rfile.read(content_length)

        js = json.loads(body.decode())
        name = "images/input/download."+js["type"]
        with open(name,"wb") as f:
            f.write(decodestring(str.encode(js["image"])))

        image = cv2.imread(name)
        result = recognize(image)
        
        self.send_response(200)
        self.end_headers()
        response = BytesIO()
        response.write(result)

        self.wfile.write(response.getvalue())


httpd = HTTPServer(('192.168.0.4', 8000), YoloServer)
httpd.serve_forever()

# curl --header "Content-Type: application/json" --request POST --data '{"type":"xyz"}' http://192.168.0.4:8000