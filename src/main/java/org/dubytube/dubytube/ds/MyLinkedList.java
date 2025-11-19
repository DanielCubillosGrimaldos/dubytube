package org.dubytube.dubytube.ds;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Implementación personalizada de una Lista Enlazada Simple (Singly Linked List).
 * Esta estructura se utiliza principalmente para almacenar las canciones favoritas de un usuario.
 * 
 * <p>Características principales:</p>
 * <ul>
 *   <li>Inserción en O(1) al inicio</li>
 *   <li>Inserción en O(1) al final (con referencia a tail)</li>
 *   <li>Búsqueda en O(n)</li>
 *   <li>Eliminación en O(n)</li>
 *   <li>Iterable para uso en bucles for-each</li>
 * </ul>
 * 
 * @param <T> Tipo de elemento almacenado en la lista
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
public class MyLinkedList<T> implements Iterable<T> {

    /**
     * Nodo interno de la lista enlazada.
     * Cada nodo contiene un dato y una referencia al siguiente nodo.
     * 
     * @param <E> Tipo de dato almacenado en el nodo
     */
    private static class Node<E> {
        E data;
        Node<E> next;

        /**
         * Constructor del nodo.
         * 
         * @param data Dato a almacenar en el nodo
         */
        Node(E data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head;  // Primer nodo de la lista
    private Node<T> tail;  // Último nodo de la lista (para inserción O(1) al final)
    private int size;      // Cantidad de elementos en la lista

    /**
     * Constructor por defecto.
     * Inicializa una lista vacía.
     */
    public MyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Agrega un elemento al inicio de la lista.
     * Complejidad: O(1)
     * 
     * @param data Elemento a agregar
     */
    public void addFirst(T data) {
        Node<T> newNode = new Node<>(data);
        
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.next = head;
            head = newNode;
        }
        size++;
    }

    /**
     * Agrega un elemento al final de la lista.
     * Complejidad: O(1) gracias a la referencia tail
     * 
     * @param data Elemento a agregar
     */
    public void addLast(T data) {
        Node<T> newNode = new Node<>(data);
        
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Agrega un elemento al final de la lista (alias de addLast).
     * Este método se incluye para compatibilidad con List de Java.
     * Complejidad: O(1)
     * 
     * @param data Elemento a agregar
     * @return true siempre (para compatibilidad con Collection)
     */
    public boolean add(T data) {
        addLast(data);
        return true;
    }

    /**
     * Inserta un elemento en una posición específica.
     * Complejidad: O(n)
     * 
     * @param index Índice donde insertar (0-based)
     * @param data Elemento a insertar
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
     */
    public void add(int index, T data) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }

        if (index == 0) {
            addFirst(data);
            return;
        }

        if (index == size) {
            addLast(data);
            return;
        }

        Node<T> newNode = new Node<>(data);
        Node<T> current = head;
        
        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }

        newNode.next = current.next;
        current.next = newNode;
        size++;
    }

    /**
     * Obtiene el elemento en una posición específica.
     * Complejidad: O(n)
     * 
     * @param index Índice del elemento (0-based)
     * @return Elemento en la posición especificada
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }

        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        return current.data;
    }

    /**
     * Obtiene el primer elemento de la lista.
     * Complejidad: O(1)
     * 
     * @return Primer elemento
     * @throws NoSuchElementException si la lista está vacía
     */
    public T getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("La lista está vacía");
        }
        return head.data;
    }

    /**
     * Obtiene el último elemento de la lista.
     * Complejidad: O(1)
     * 
     * @return Último elemento
     * @throws NoSuchElementException si la lista está vacía
     */
    public T getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("La lista está vacía");
        }
        return tail.data;
    }

    /**
     * Elimina el primer elemento de la lista.
     * Complejidad: O(1)
     * 
     * @return Elemento eliminado
     * @throws NoSuchElementException si la lista está vacía
     */
    public T removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("La lista está vacía");
        }

        T data = head.data;
        head = head.next;
        size--;

        if (isEmpty()) {
            tail = null;
        }

        return data;
    }

    /**
     * Elimina el último elemento de la lista.
     * Complejidad: O(n) - requiere recorrer hasta el penúltimo nodo
     * 
     * @return Elemento eliminado
     * @throws NoSuchElementException si la lista está vacía
     */
    public T removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("La lista está vacía");
        }

        if (size == 1) {
            return removeFirst();
        }

        Node<T> current = head;
        while (current.next != tail) {
            current = current.next;
        }

        T data = tail.data;
        current.next = null;
        tail = current;
        size--;

        return data;
    }

    /**
     * Elimina la primera ocurrencia del elemento especificado.
     * Complejidad: O(n)
     * 
     * @param data Elemento a eliminar
     * @return true si el elemento fue encontrado y eliminado, false en caso contrario
     */
    public boolean remove(T data) {
        if (isEmpty()) {
            return false;
        }

        // Caso especial: el elemento está en head
        if (head.data.equals(data)) {
            removeFirst();
            return true;
        }

        Node<T> current = head;
        while (current.next != null) {
            if (current.next.data.equals(data)) {
                // Si es el tail, actualizarlo
                if (current.next == tail) {
                    tail = current;
                }
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }

        return false;
    }

    /**
     * Elimina el elemento en la posición especificada.
     * Complejidad: O(n)
     * 
     * @param index Índice del elemento a eliminar
     * @return Elemento eliminado
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
     */
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }

        if (index == 0) {
            return removeFirst();
        }

        Node<T> current = head;
        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }

        T data = current.next.data;
        
        if (current.next == tail) {
            tail = current;
        }
        
        current.next = current.next.next;
        size--;

        return data;
    }

    /**
     * Verifica si la lista contiene el elemento especificado.
     * Complejidad: O(n)
     * 
     * @param data Elemento a buscar
     * @return true si el elemento está en la lista, false en caso contrario
     */
    public boolean contains(T data) {
        Node<T> current = head;
        while (current != null) {
            if (current.data.equals(data)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Busca el índice de la primera ocurrencia del elemento especificado.
     * Complejidad: O(n)
     * 
     * @param data Elemento a buscar
     * @return Índice del elemento, o -1 si no se encuentra
     */
    public int indexOf(T data) {
        Node<T> current = head;
        int index = 0;
        
        while (current != null) {
            if (current.data.equals(data)) {
                return index;
            }
            current = current.next;
            index++;
        }
        
        return -1;
    }

    /**
     * Elimina todos los elementos de la lista.
     * Complejidad: O(1)
     */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Retorna el número de elementos en la lista.
     * Complejidad: O(1)
     * 
     * @return Cantidad de elementos
     */
    public int size() {
        return size;
    }

    /**
     * Verifica si la lista está vacía.
     * Complejidad: O(1)
     * 
     * @return true si la lista no contiene elementos
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Aplica una acción a cada elemento de la lista.
     * Complejidad: O(n)
     * 
     * @param action Acción a aplicar a cada elemento
     */
    public void forEach(Consumer<? super T> action) {
        Node<T> current = head;
        while (current != null) {
            action.accept(current.data);
            current = current.next;
        }
    }

    /**
     * Convierte la lista a un array de Object.
     * Complejidad: O(n)
     * 
     * @return Array con todos los elementos de la lista
     */
    public Object[] toArray() {
        Object[] array = new Object[size];
        Node<T> current = head;
        int index = 0;
        
        while (current != null) {
            array[index++] = current.data;
            current = current.next;
        }
        
        return array;
    }

    /**
     * Retorna un iterador para recorrer la lista.
     * Permite usar la lista en bucles for-each.
     * 
     * @return Iterador de la lista
     */
    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    /**
     * Iterador interno para recorrer la lista.
     */
    private class LinkedListIterator implements Iterator<T> {
        private Node<T> current = head;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T data = current.data;
            current = current.next;
            return data;
        }
    }

    /**
     * Representación en String de la lista.
     * Formato: [elemento1, elemento2, elemento3]
     * 
     * @return String representando la lista
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        Node<T> current = head;
        
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        
        sb.append("]");
        return sb.toString();
    }

    /**
     * Invierte el orden de los elementos en la lista.
     * Complejidad: O(n)
     */
    public void reverse() {
        if (size <= 1) {
            return;
        }

        Node<T> prev = null;
        Node<T> current = head;
        tail = head; // El head actual será el nuevo tail
        
        while (current != null) {
            Node<T> next = current.next;
            current.next = prev;
            prev = current;
            current = next;
        }
        
        head = prev;
    }

    /**
     * Crea una copia superficial de la lista.
     * Complejidad: O(n)
     * 
     * @return Nueva lista con los mismos elementos
     */
    public MyLinkedList<T> clone() {
        MyLinkedList<T> cloned = new MyLinkedList<>();
        Node<T> current = head;
        
        while (current != null) {
            cloned.addLast(current.data);
            current = current.next;
        }
        
        return cloned;
    }
}
