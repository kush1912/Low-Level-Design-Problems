package lld.DesignProblems.pragmatic;


/*
[3:14 PM] Pavan Muthukuru
Given a pointer to the head node of a linked list, the task is to reverse the linked list. We need to reverse the list by changing the links between nodes.
Examples:
Input: Head of following linked list
1->2->3->4->NULL
Output: Linked list should be changed to,
4->3->2->1->NULL
Input: Head of following linked list
1->2->3->4->5->NULL
Output: Linked list should be changed to,
5->4->3->2->1->NULL


//Inputs:   1->2->3->4->5->6->7->8->9->NULL and k = 3
// Output:   3->2->1->4->5->6->9->8->7->NULL.

*/

public class LinkedList {
        static Node head;
        static class Node {
            int data;
            Node next;
            Node(int d)
            {
                data = d;
                next = null;
            }
        }
        /* Function to reverse the linked list */
        Node reverse(Node head)
        {
            Node prev= null;
            Node current = head;
            Node next =null;
            while(current!=null) {
                next = current.next;
                current.next=prev;
                prev=current;
                current= next;
            }
            head = prev;
            return head;
        }

        Node reverse(Node head , int k){
            Node current =head;
            Node prev = null;
            Node next = null;
            int c=0;
            while(current!=null && c<k){
                next = current.next;
                current.next = prev;
                prev = current;
                current=next;
                c++;
            }
            Node temp = next;
            head.next = temp;
            Node prevTemp=null;
            while(temp!=null && c>0){
                prevTemp = temp;
                temp = temp.next;
                c--;
            }
            if(temp!=null){
                prevTemp.next= reverse(temp, k);
            }
            return prev;
        }

        // prints content of double linked list

        void printList(Node node)
        {
            while (node != null) {
                System.out.print(node.data + " ");
                node = node.next;
            }
        }

        // Driver Code
        public static void main(String[] args)
        {
            LinkedList list = new LinkedList();
            list.head = new Node(1);
            list.head.next = new Node(2);
            list.head.next.next = new Node(3);
            list.head.next.next.next = new Node(4);
            list.head.next.next.next.next = new Node(5);
            list.head.next.next.next.next.next = new Node(6);
            list.head.next.next.next.next.next.next = new Node(7);
            list.head.next.next.next.next.next.next.next = new Node(8);
            list.head.next.next.next.next.next.next.next.next = new Node(9);
            System.out.println("Given linked list");
            list.printList(head);
//            head = list.reverse(head);
            head = list.reverse(head, 3);
            System.out.println("");
            System.out.println("Reversed linked list ");
            list.printList(head);

        }

}

