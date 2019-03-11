CardReader
=======

Программа распознает карты, которые лежат на столе (по центру картинки).

## Принцип работы:

    1. Заполняем колоду карт из ресурса deck.yml, где ключи это имена карты 
        и каждому ключу соответствует массив pHsah (отпечаток картинки см. http://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html).
        Чем больше количество pHsah для каждой карты тем более точное распознавание этой карты 
    2. Получаем директорию с картинками из входящего аргумента
    3. Из каждой картинки (png, jpg, gif) извлекаем массив карт-картинок
    4. Для каждой извлечённой карты вычисляем pHsah
    5. Для каждой карты в колоде вычисляем количество различных байт между pHsah карты в колоде и pHsah извлечённой карты
    6. Выбираем карту из колоды с минимально отличающимися pHsah и выводим ее ключ в консоль
             
    
> **NOTE:** Если разница между pHsah изображений более 5 байт - картинки разные.


## Как использовать

_Вариант 1:_ Укажите в файле run.cmd для переменной **source_dir** директорию с картинками, и запустите run.cmd.

_Вариант 2:_ Скопируйте фал CardReader.jar в директорию с картинками и из директории выполните команду **java -jar CardReader.jar**

_Вариант 3:_ Выполните команду **java -jar CardReader.jar %source_dir%** где %source_dir% это полный путь до директории с картинками. Пример: **java -jar CardReader.jar "e:\java\java_test_task\imgs"**   