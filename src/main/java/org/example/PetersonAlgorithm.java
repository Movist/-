package org.example;

import java.io.*;

public class PetersonAlgorithm {
    public static void main(String[] args) {
        String fileName = "numbers.txt";
        final PetersonMutex mutex = new PetersonMutex();

        // Создание файла, если его нет
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Создание и запуск потока
        Thread writerReaderThread = new Thread(() -> {
            try {
                // Первый проход: запись чисел, не делящихся на 3 в файл
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                for (int i = 1; i <= 100; i++) {
                    if (i % 3 != 0) {
                        mutex.lock(0); // Блокировка первого потока
                        writer.write(Integer.toString(i));
                        writer.newLine();
                        mutex.unlock(0); // Разблокировка первого потока
                    }
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Второй проход: чтение чисел из файла и удаление четных
        Thread readerThread = new Thread(() -> {
            try {
                File inputFile = new File(fileName);
                File tempFile = new File("temp.txt");
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                String line;
                while ((line = reader.readLine()) != null) {
                    int number = Integer.parseInt(line);
                    if (number % 2 != 0) {
                        writer.write(Integer.toString(number));
                        writer.newLine();
                    }
                }

                reader.close();
                writer.close();

                if (inputFile.delete() && tempFile.renameTo(inputFile)) {
                    System.out.println("Файл успешно обновлен.");
                } else {
                    System.out.println("Не удалось обновить файл.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        writerReaderThread.start();
        readerThread.start();

        try {
            writerReaderThread.join();
            readerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class PetersonMutex {
    private boolean[] flag = new boolean[2];
    private int victim;

    public void lock(int threadId) {
        int otherThread = 1 - threadId;
        flag[threadId] = true;
        victim = threadId;
        while (flag[otherThread] && victim == threadId) {
            // Ждем, пока другой поток не разблокируется
        }
    }

    public void unlock(int threadId) {
        flag[threadId] = false;
    }
}
