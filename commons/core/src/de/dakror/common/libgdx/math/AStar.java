package de.dakror.common.libgdx.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class AStar<T> {
    public static interface Visitor<T> {
        void visit(T element);
    }

    private class Node {
        private Node parent;
        private float g;
        private float h;
        private T data;

        Node(T data, Node parent) {
            this.data = data;
            setParent(parent);
        }

        void setParent(Node parent) {
            this.parent = parent;
            if (parent == null) g = 0;
            else g = parent.g + network.getEdgeLength(parent.data, data);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass().equals(getClass()) && hashCode() == obj.hashCode();
        }

        @Override
        public String toString() {
            return String.format("{%s, g=%f, h=%f}", data.toString(), g, h);
        }
    }

    public abstract static class Network<T> {
        public abstract float getH(T start, T end);

        public abstract float getEdgeLength(T start, T end);

        public abstract void visitNeighbors(T node, T start, T end, Visitor<T> visitor);
    }

    LinkedList<Node> openList;
    HashSet<Node> closedList;
    Network<T> network;
    T finish;

    Comparator<Node> comparator;

    long maxTime;

    public AStar() {
        openList = new LinkedList<>();
        closedList = new HashSet<>();
        comparator = new Comparator<AStar<T>.Node>() {
            @Override
            public int compare(AStar<T>.Node a, AStar<T>.Node b) {
                return Float.compare(a.g + a.h, b.g + b.h);
            }
        };
    }

    private void neighborVisitor(Node parent, T n) {
        Node node = new Node(n, parent);
        if (closedList.contains(node)) return;
        int index = openList.indexOf(node);
        if (index > -1) {
            Node old = openList.get(index);
            if (old.g > node.g) {
                old.setParent(parent);
            }
        } else {
            node.h = network.getH(n, finish);
            openList.add(node);
        }
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public LinkedList<T> findPath(Network<T> network, T start, T finish) {
        this.network = network;

        LinkedList<T> path = new LinkedList<>();
        if (start == finish) {
            path.add(start);
            return path;
        }

        this.finish = finish;

        openList.clear();
        closedList.clear();
        Node startNode = new Node(start, null);
        startNode.h = network.getH(start, finish);

        openList.add(startNode);

        long now = System.nanoTime();
        while (!openList.isEmpty()) {
            if (maxTime > 0 && System.nanoTime() - now > maxTime * 1_000_000) {
                break;
            }

            Collections.sort(openList, comparator);
            final Node n = openList.poll();

            if (n.data.equals(finish)) {
                Node t = n;
                while (t != null) {
                    path.add(t.data);
                    t = t.parent;
                }

                Collections.reverse(path);
                return path;
            }

            closedList.add(n);
            network.visitNeighbors(n.data, start, finish, new Visitor<T>() {
                @Override
                public void visit(T x) {
                    neighborVisitor(n, x);
                }
            });
        }

        return path;
    }

    public List<T> getOpenList() {
        List<T> l = new ArrayList<>();
        for (Node n : openList)
            l.add(n.data);
        return l;
    }

    public List<T> getClosedList() {
        List<T> l = new ArrayList<>();
        for (Node n : closedList)
            l.add(n.data);
        return l;
    }
}
