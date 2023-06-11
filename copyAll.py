import os
import clipboard

dirs = [
    '/home/yehuda/YEHUDA/Desktop/brain-battle/app/src/main/java/com/example/trivia/',
    '/home/yehuda/YEHUDA/Desktop/brain-battle/app/src/main/res/layout/',
    '/home/yehuda/YEHUDA/Desktop/brain-battle/app/src/main/res/values/'
]

text_to_copy = ''

for dir in dirs:
    for file_path in os.listdir(dir):
        file = open(dir+file_path)

        text_to_copy += f'\n\n\n:{file_path}:\n'
        text_to_copy += file.read()

        file.close()

clipboard.copy(text_to_copy)
print(text_to_copy)
