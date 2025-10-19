package ru.daniil.NauJava.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.daniil.NauJava.entity.Product;
import ru.daniil.NauJava.service.ProductService;

//@Component
public class CommandProcessor
{
    //private final ProductService productService;

    //@Autowired
    public CommandProcessor()//ProductService productService)
    {
        //this.productService = productService;
    }

    /**
     * Метод, что принимает на входе строку состоящую из:
     * действия и параметров к нему разделённых пробелом.
     * Используется для работы с productService через консоль.
     * Ничего не возвращает, но выводит результаты в консоль.
     * @param input строка состоящая из действия и параметров к нему разделённых пробелом
     */
    public void processCommand(String input)
    {
        /*String[] cmd = input.split(" ");
        switch (cmd[0])
        {
            case "create" ->
            {
                Product item = productService.findById(Long.valueOf(cmd[1]));
                if (item == null) {
                    productService.createProduct(
                            Long.valueOf(cmd[1]),
                            cmd[2],
                            cmd[3],
                            Double.parseDouble(cmd[4])
                    );
                    System.out.println("Продукт успешно добавлен...");
                } else {
                    System.out.println("Продукт с ID " + cmd[1] + " уже существует.");
                }
            }
            case "read" ->
            {
                Product item = productService.findById(Long.valueOf(cmd[1]));
                if (item != null) {
                    System.out.printf("Данные по продукту:\n" +
                                    "ID: %s \nНазвание: %s \nОписание: %s \nКалории: %s \n",
                            item.getId(), item.getName(), item.getDescription(),  item.getCaloriesPer100g());
                } else {
                    System.out.println("Продукт с ID " + cmd[1] + " не найден.");
                }
            }
            case "update-description" ->
            {
                productService.updateDescription(
                        Long.valueOf(cmd[1]),
                        cmd[2]
                );
                System.out.println("Продукт успешно изменён...");
            }
            case "update-calories" ->
            {
                productService.updateCalories(
                        Long.valueOf(cmd[1]),
                        Double.parseDouble(cmd[2])
                );
                System.out.println("Продукт успешно изменён...");
            }
            case "delete" ->
            {
                productService.deleteById(
                        Long.valueOf(cmd[1])
                );
                System.out.println("Продукт успешно удалён...");
            }

            default -> System.out.println("Введена неизвестная команда...");
        }*/
    }
}
