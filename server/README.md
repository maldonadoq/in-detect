# YOLO with OpenCV and Python 

OpenCV `dnn` module supports running inference on pre-trained deep learning models from popular frameworks like Caffe, Torch and TensorFlow. 

When it comes to object detection, popular detection frameworks are
 * YOLO
 * SSD
 * Faster R-CNN

 Support for running YOLO/DarkNet has been added to OpenCV dnn module recently. 

 ## Dependencies
  * opencv
  * numpy
  * python 3

`pip install numpy opencv-python`

 ## You Only Look Once

 Download the pre-trained YOLO v3 weights file from this [link](https://pjreddie.com/media/files/yolov3.weights) and place it in the current directory or you can directly download to the current directory in terminal using

 `$ wget https://pjreddie.com/media/files/yolov3.weights`

 Provided all the files are in the current directory, below command will apply object detection on the input image `dog.jpg`.

 `$ python yolo_opencv.py -c yolov3.cfg -w yolov3.weights -cl yolov3.txt -i images/input/13.jpg`

## Server

Run 
`$ python server`