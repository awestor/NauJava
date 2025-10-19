package ru.daniil.NauJava.repository;

public interface CrudRepositoryTask3<T, ID>  {
    /**
     * Метод, что создаёт новую запись продукта
     * @param entity экземпляр сущности
     */
    void create(T entity);

    /**
     * Метод, что находит продукт в списке по его id
     * @param id идентификатор сущности в БД
     * @return экземпляр сущности
     */
    T read(ID id);

    /**
     * Метод, что изменяет данные продукта, используя
     * его id для нахождения в списке
     * @param entity экземпляр сущности
     */
    void update(T entity);

    /**
     * Метод, что находит и удаляет продукт по его id
     * @param id идентификатор сущности в БД
     */
    void delete(ID id);
}