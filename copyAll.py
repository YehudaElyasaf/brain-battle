import os

dirs = [
    '/home/yehuda/YEHUDA/Desktop/brain-battle/app/src/main/java/com/example/trivia/',
    '/home/yehuda/YEHUDA/Desktop/brain-battle/app/src/main/res/layout/',
    '/home/yehuda/YEHUDA/Desktop/brain-battle/app/src/main/res/values/'
]

for dir in dirs:
    for filePath in os.listdir(dir):
        file = open(dir+filePath)

        print(f'{file}:')
        print(file.read())

        file.close()
