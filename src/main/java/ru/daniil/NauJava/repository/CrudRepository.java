package ru.daniil.NauJava.repository;

public interface CrudRepository<T, ID>  {
    /**
     * Метод, что создаёт новую запись продукта
     * @param entity
     */
    void create(T entity);

    /**
     * Метод, что находит продукт в списке по его id
     * @param id
     * @return
     */
    T read(ID id);

    /**
     * Метод, что изменяет данные продукта, используя
     * его id для нахождения в списке
     * @param entity
     */
    void update(T entity);

    /**
     * Метод, что находит и удаляет продукт по его id
     * @param id
     */
    void delete(ID id);
}