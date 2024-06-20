package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {
    private static final int alphabetSize = 62;
    private Node<Value> root;

    private class Node<Value>{
        private Set<Value> vals = new HashSet<>();
        private Node[] links = new Node[alphabetSize];

        private boolean hasChildren()
        {
            for(Node x: this.links)
            {
                if(x!=null)
                    return true;
            }
            return false;
        }
        private void setVals(Set<Value> vals)
        {
            this.vals=vals;
        }
        private boolean hasChildrenWithVals()
        {
            if(this.hasChildren())
            {
                for(Node x: this.links)
                {
                    if(x!=null && !x.vals.isEmpty())
                        return true;
                }
            }
            return false;
        }

    }
    public TrieImpl()
    {
        root= new Node<>();
    }
    private String stripPunctuation(String str)
    {
        String newStr = "";
        char[] keyArr = str.toCharArray();
        for(int i =0; i<keyArr.length;i++)
        {
            if(Character.isDigit(keyArr[i]) || Character.isLetter(keyArr[i]))
            {
                newStr+=keyArr[i];
            }
        }
        return newStr;
    }
    private int toFitInArr(int n)
    {
        if(48<=n && n<=57)
            return n-48;
        else if(65<=n && n<=90)
            return (n%(root.links.length))+7;
        else if(97<=n && n<=122)
            return (n%(root.links.length))+1;
        else
            throw new IllegalArgumentException("num is not a letter or number");
    }
    @Override
    public void put(String key, Value val)
    {
        key=stripPunctuation(key);
        if(val==null)
        {
            this.deleteAll(key);
        }
        else
        {
            this.root=put(this.root,key,val,0);
        }
    }
    private Node put(Node x,String key, Value val, int d)
    {
        if(x==null)
        {
            x=new Node();

        }
        if(d==key.length())
        {
            x.vals.add(val);
            return x;
        }
        char c = key.charAt(d);
        x.links[this.toFitInArr(c)]= this.put(x.links[this.toFitInArr(c)],key,val,d+1);
        return x;
    }

    @Override
    public Set<Value> get(String key)
    {
        try
        {
            if((getNode(key, this.root, 0)!=null))
                return getNode(key, this.root, 0).vals;
            else
                return new HashSet<>();
        }
        catch (IllegalArgumentException e)
        {
            return new HashSet<>();
        }


    }
    private void removeNodesWithNoChildren(Node x) {
        if (x != null) {
            for (Node current : x.links) {
                if (current != null && current.hasChildren()) {
                    removeNodesWithNoChildren(current);
                }
                if (current != null && current.vals.isEmpty() && !current.hasChildren()) {
                    for (int i = 0; i < x.links.length; i++) {
                        if (x.links[i] == current) {
                            x.links[i] = null;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public Value delete(String key, Value val) {

        try{
            Set<Value> valsAtKey = this.get(key);

            if(valsAtKey.contains(val))
            {
                valsAtKey.remove(val);
                if(valsAtKey.isEmpty()){
                    removeNodesWithNoChildren(this.root);
                }
                return val;
            }
            else {
                return null;
            }
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }

    }
    @Override
    public Set<Value> deleteAll(String key)
    {
        try{
            Set<Value> vals= this.get(key);
            HashSet<Value> output= new HashSet<>(vals);
            vals.clear();
            if(this.getNode(key, this.root,0)!=null)
                this.getNode(key, this.root,0).setVals(vals);
            removeNodesWithNoChildren(this.root);
            return output;
        }
        catch (IllegalArgumentException e )
        {
            return new HashSet<>();
        }

    }
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix)
    {
        try
        {
            Node nodeAtPrefix = getNode(prefix, this.root, 0);
            Set<Value> output = new HashSet<>();
            deleteAllWithPrefix(prefix, output, nodeAtPrefix);
            return output;
        }
        catch (IllegalArgumentException e)
        {
            return new HashSet<>();
        }

    }
    private void deleteAllWithPrefix(String prefix, Set<Value> vals, Node x)
    {
        if(x!=null ) {
            if(x!=root)
            {
                Set<Value> deleted = deleteAll(prefix);
                vals.addAll(deleted);
                if (!x.hasChildren())
                    return;
            }


            for (int i = 0; i < x.links.length; i++) {

                if (x.links[this.toFitInArr(getChar(i))] != null) {
                    deleteAllWithPrefix(prefix + getChar(i), vals, x.links[this.toFitInArr(getChar(i))]);
                }

            }
        }

    }
    private char getChar(int n)
    {
        if(0>= n || 9>=n)
        {
            return (char)(n+48);
        }
        else if(10<=n && n<=35)
            return (char)(n+55);
        else if(36<=n && n<=61)
            return (char)(n+61);
        else
            throw new IllegalArgumentException("num is not a letter or number");
    }
    private Node getNode(String str, Node next, int d)
    {
        if(next==null)
            return null;
        else if(d==str.length())
            return next;

        char c = str.charAt(d);
        return getNode(str, next.links[this.toFitInArr(c)], d+1);
    }
    @Override
    public List<Value> getSorted(String key, Comparator<Value> comparator) {
        try
        {
            List<Value> output = new ArrayList<>();
            output.addAll(get(key));
            Collections.sort(output,comparator);
            return output;
        }
        catch (IllegalArgumentException e)
        {
            return new ArrayList<>();
        }

    }

    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator)
    {

        try{
            List<Value> output = new ArrayList<>();
            Node prefixLocation = getNode(prefix, this.root,0);
            if(getNode(prefix, this.root,0)!= null){
                output.addAll(prefixLocation.vals);
            }
            if(prefixLocation!= null && prefixLocation.hasChildren())
            {
                getAllWithPrefixSorted(prefix, getNode(prefix, this.root, 0),output);
            }
            Set<Value> setOutput = new HashSet<>(output);
            output= new ArrayList<>(setOutput);
            Collections.sort(output,comparator);
            return output;
        }
        catch (IllegalArgumentException e)
        {
            return new ArrayList<>();
        }
    }
    private void getAllWithPrefixSorted(String prefix, Node x, List<Value> output)
    {

        if(x!=null) {
            output.addAll(get(prefix));
            if (!x.hasChildren())
                return;


            for (int i = 0; i < x.links.length; i++) {

                if (x.links[this.toFitInArr(getChar(i))] != null) {
                    getAllWithPrefixSorted(prefix + getChar(i), x.links[this.toFitInArr(getChar(i))], output);


                }

            }
        }
    }

}
