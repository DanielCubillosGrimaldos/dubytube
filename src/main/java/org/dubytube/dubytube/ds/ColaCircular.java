package org.dubytube.dubytube.ds;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Cola Circular (Circular Queue) para implementar reproductor tipo radio.
 * 
 * <p>Esta estructura permite reproducción continua en loop infinito,
 * ideal para un reproductor de tipo "radio" donde las canciones se
 * reproducen cíclicamente sin fin.</p>
 * 
 * <p><b>Características:</b></p>
 * <ul>
 *   <li>Basada en array circular de tamaño fijo</li>
 *   <li>Operaciones O(1) para enqueue, dequeue, peek</li>
 *   <li>Modo radio: al llegar al final, vuelve al inicio automáticamente</li>
 *   <li>Soporta shuffle (orden aleatorio)</li>
 * </ul>
 * 
 * @param <T> Tipo de elementos en la cola
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
public class ColaCircular<T> implements Iterable<T> {
    
    private Object[] elementos;
    private int frente;
    private int rear;
    private int size;
    private int capacidad;
    private boolean modoRadio; // Si está en true, next() vuelve al inicio al llegar al final

    /**
     * Constructor con capacidad especificada.
     * 
     * @param capacidad Capacidad máxima de la cola
     */
    public ColaCircular(int capacidad) {
        this.capacidad = capacidad;
        this.elementos = new Object[capacidad];
        this.frente = 0;
        this.rear = -1;
        this.size = 0;
        this.modoRadio = true; // Por defecto, modo radio activado
    }

    /**
     * Constructor con capacidad por defecto (100).
     */
    public ColaCircular() {
        this(100);
    }

    /**
     * Agrega un elemento al final de la cola.
     * Complejidad: O(1)
     * 
     * @param elemento Elemento a agregar
     * @return true si se agregó, false si la cola está llena
     */
    public boolean enqueue(T elemento) {
        if (isFull()) {
            return false;
        }
        
        rear = (rear + 1) % capacidad;
        elementos[rear] = elemento;
        size++;
        return true;
    }

    /**
     * Elimina y retorna el elemento al frente de la cola.
     * Complejidad: O(1)
     * 
     * @return Elemento eliminado
     * @throws NoSuchElementException si la cola está vacía
     */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola está vacía");
        }
        
        T elemento = (T) elementos[frente];
        elementos[frente] = null;
        frente = (frente + 1) % capacidad;
        size--;
        return elemento;
    }

    /**
     * Retorna el elemento al frente sin eliminarlo.
     * Complejidad: O(1)
     * 
     * @return Elemento al frente
     * @throws NoSuchElementException si la cola está vacía
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola está vacía");
        }
        return (T) elementos[frente];
    }

    /**
     * Avanza al siguiente elemento en modo radio.
     * Si está al final, vuelve al inicio automáticamente.
     * 
     * @return Siguiente elemento
     * @throws NoSuchElementException si la cola está vacía
     */
    @SuppressWarnings("unchecked")
    public T next() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola está vacía");
        }
        
        T actual = (T) elementos[frente];
        
        if (modoRadio) {
            // En modo radio, avanzar circularmente
            frente = (frente + 1) % capacidad;
            
            // Si llegamos al final, volver al inicio
            if (frente == (rear + 1) % capacidad) {
                frente = 0;
            }
        } else {
            // Modo normal, solo avanzar si no estamos al final
            if (frente != rear) {
                frente = (frente + 1) % capacidad;
            }
        }
        
        return actual;
    }

    /**
     * Retorna al elemento anterior en modo radio.
     * 
     * @return Elemento anterior
     * @throws NoSuchElementException si la cola está vacía
     */
    @SuppressWarnings("unchecked")
    public T previous() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola está vacía");
        }
        
        frente = (frente - 1 + capacidad) % capacidad;
        return (T) elementos[frente];
    }

    /**
     * Reinicia la posición al primer elemento.
     */
    public void reset() {
        if (!isEmpty()) {
            // Encontrar el índice real del primer elemento
            frente = findFirstIndex();
        }
    }

    /**
     * Encuentra el índice del primer elemento insertado.
     */
    private int findFirstIndex() {
        if (size == capacidad) {
            return (rear + 1) % capacidad;
        }
        return 0;
    }

    /**
     * Obtiene el elemento en una posición específica.
     * 
     * @param index Índice del elemento (0-based desde el frente)
     * @return Elemento en esa posición
     * @throws IndexOutOfBoundsException si el índice es inválido
     */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice: " + index + ", Tamaño: " + size);
        }
        
        int realIndex = (frente + index) % capacidad;
        return (T) elementos[realIndex];
    }

    /**
     * Mezcla aleatoriamente los elementos (shuffle).
     * Útil para modo aleatorio en el reproductor.
     */
    public void shuffle() {
        if (size <= 1) {
            return;
        }
        
        java.util.Random random = new java.util.Random();
        
        for (int i = size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            
            int indexI = (frente + i) % capacidad;
            int indexJ = (frente + j) % capacidad;
            
            // Intercambiar elementos[indexI] y elementos[indexJ]
            Object temp = elementos[indexI];
            elementos[indexI] = elementos[indexJ];
            elementos[indexJ] = temp;
        }
    }

    /**
     * Activa o desactiva el modo radio.
     * 
     * @param modoRadio true para activar modo radio
     */
    public void setModoRadio(boolean modoRadio) {
        this.modoRadio = modoRadio;
    }

    /**
     * Verifica si el modo radio está activo.
     * 
     * @return true si está en modo radio
     */
    public boolean isModoRadio() {
        return modoRadio;
    }

    /**
     * Verifica si la cola está vacía.
     * 
     * @return true si está vacía
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Verifica si la cola está llena.
     * 
     * @return true si está llena
     */
    public boolean isFull() {
        return size == capacidad;
    }

    /**
     * Obtiene el número de elementos en la cola.
     * 
     * @return Tamaño de la cola
     */
    public int size() {
        return size;
    }

    /**
     * Obtiene la capacidad máxima de la cola.
     * 
     * @return Capacidad
     */
    public int capacity() {
        return capacidad;
    }

    /**
     * Limpia todos los elementos de la cola.
     */
    public void clear() {
        elementos = new Object[capacidad];
        frente = 0;
        rear = -1;
        size = 0;
    }

    /**
     * Convierte la cola a una lista para visualización.
     * 
     * @return Lista con los elementos en orden
     */
    public java.util.List<T> toList() {
        java.util.List<T> lista = new java.util.ArrayList<>();
        for (T elemento : this) {
            lista.add(elemento);
        }
        return lista;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int count = 0;

            @Override
            public boolean hasNext() {
                return count < size;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                
                int index = (frente + count) % capacidad;
                count++;
                return (T) elementos[index];
            }
        };
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            int index = (frente + i) % capacidad;
            sb.append(elementos[index]);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
