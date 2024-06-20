package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

import java.util.Arrays;

public class StackImpl<T> implements Stack<T>{

    private int top;
    private T[] data;

    public StackImpl()
    {
        this.data = (T[]) new Object[4];
        this.top=-1;
    }

    public void push(T element)
    {
        if(element==null)
            throw new IllegalArgumentException("Cant push null elemnet onto stack");
        if(this.top==data.length-1)
        {
            T[] dataCopy = Arrays.copyOf(this.data, this.data.length);
            this.data = (T[]) new Object[dataCopy.length * 2];

            for(int i =0; i<dataCopy.length; i++)
            {
                this.data[i]=dataCopy[i];
            }
        }
        this.data[this.top+1]= element;
        this.top++;

    }
    public T pop()
    {
        if(this.top==-1)
        {
            return null;
        }

        T item = this.data[this.top];
        this.data[this.top] =null;
        this.top--;
        return item;
    }
    public T peek()
    {
        if(top==-1)
        {
            return null;
        }
        return this.data[this.top];
    }
    public int size()
    {
        return this.top+1;
    }

}
