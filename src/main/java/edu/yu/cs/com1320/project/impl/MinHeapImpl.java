package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {

    public MinHeapImpl()
    {
        elements = (E[]) (new Comparable[8]);
    }
    @Override
    public void reHeapify(Comparable element) {
        upHeap(getArrayIndex(element));
        downHeap(getArrayIndex(element));

    }

    @Override
    protected int getArrayIndex(Comparable element) {
        for (int i=1;i<elements.length; i++)
        {
            if(this.elements[i]!=null &&elements[i].equals(element))
                return i;
        }
        throw new NoSuchElementException("element not in heap");
    }

    @Override
    protected void doubleArraySize()
    {
        E[] newElements = (E[]) new Comparable[elements.length*2];
        for(int i =1; i<elements.length;i++)
        {
            newElements[i]= elements[i];
        }

        elements= newElements;
    }
}
