package com.scrabblegame.scrabbleserver;

import java.net.*;
import java.io.*;
import java.util.HashMap;

/**
 * THIS IS CODE RUNNING ON SERVER MACHINE
 * 
 * @author Stephen He 
 * @author Victor Ouy
 * @author Lucas
 */
public class ServerConnection {

    private static final int BUFSIZE = 23;   // Size of receive buffer
    private static ServerGameState serverGameState;

    /**
     * Receives and sends bytes to client
     * 
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {

        int servPort = Integer.parseInt("50000");

        // Create a server socket to accept client connection requests
        ServerSocket servSock = new ServerSocket(servPort);

        int recvMsgSize;   // Size of received message
        byte[] receiveBuf = new byte[BUFSIZE];  // Receive buffer

        System.out.println("IP Address: " + java.net.InetAddress.getLocalHost().toString());
        serverGameState = new ServerGameState();

        while (true) { // Run forever, accepting and servicing connections
            Socket clntSock = servSock.accept();     // Get client connection
            SocketAddress clientAddress = clntSock.getRemoteSocketAddress();

            InputStream in = clntSock.getInputStream();
            OutputStream out = clntSock.getOutputStream();

            // Receive until client closes connection, indicated by -1 return
            while ((recvMsgSize = in.read(receiveBuf)) != -1) {
                byte[] sendByte = new byte[BUFSIZE];
                switch (receiveBuf[0]) {
                    //Client plays
                    case 0:
                        // If it is 0, then this play is a PASS so you skip this step
                        if (receiveBuf[1] != 0) {
                            //First we update the board and calculate score
                            serverGameState.processClientPlay(receiveBuf);
                        }
                        
                        //AI moves
                        sendByte = serverGameState.aiMoves();
                        out.write(sendByte, 0, recvMsgSize);
                        
                        //Package all the game states not related to board, and send
                        sendByte = serverGameState.sendUpdateGameState(receiveBuf);
                        out.write(sendByte, 0, recvMsgSize);
                        break;
                    //Client swaps
                    case 1:
                        //AI moves
                        sendByte = serverGameState.aiMoves();
                        out.write(sendByte, 0, recvMsgSize);
                        
                        // Calls for client letter swaps
                        sendByte = serverGameState.sendUpdateGameState(receiveBuf);
                        out.write(sendByte, 0, recvMsgSize);
                        break;
                    //Client gets start hand
                    case 2:
                        serverGameState = new ServerGameState();
                        // Calls for client start letter rack
                        sendByte = serverGameState.sendUpdateGameState(receiveBuf);
                        out.write(sendByte, 0, recvMsgSize);
                        break;
                }
            }
            clntSock.close();  // Close the socket.  We are done with this client!
        }
    }
}
Q1:

Q1- (2marks) define a function-based view called home_view: 

the template page is called 'home.html' and its tab title is called 'home'
it renders the template page together with the aforementioned title
import the necessary module that allows the view to render the home.html.
def home_view(request):

     context['title'] = 'home'

     return render(request, 'Evalutation/home.html', context)



Q2- (2marks) - import the necessary class ListView and define a class-based view called AssessmentListView that uses ListView: 

the list is based on the assessment duration in an ascending order
notice the title all assessments
use the default Django naming for the template.

from django.views.generic import ListView
class AssessmentListView(ListView):
    model = Assessment

    template_name = 'Evaluation/assessment_list.html'

    

    def query_set(self):

         return Assessment.objects.all().order_by('duration')




  def get_context_data(self, **kwargs):
    context = super().get_context_data(**kwargs)
    context['title'] = 'all assessments'
    return context


list all questionsQ3- (3 marks) - Define a class-based view called QuestionListView that uses ListView: 


from django.views.generic import ListView
class QuestionListView(ListView): 
    model = Question

    paginate_by = 10
    template_name = 'Evaluation/question_list.html'




    def query_set(self):

         return Question.objects.all().order_by('assessment__name')




  def get_context_data(self, **kwargs):
    context = super().get_context_data(**kwargs)
    context['title'] = 'all questions'
    return context


Q4- (4.5 marks) --Define a class-based view called FinalQuestionsListView that uses ListView: 


from django.views.generic import ListView
class FinalQuestionsListView(ListView): 

    model = Question

    paginate_by = 10
    template_name = 'Evaluation/finalquestion_list.html'




    def query_set(self):

         return Question.objects.filter(assessment__name='final').order_by('assessment__name')




  def get_context_data(self, **kwargs):
    context = super().get_context_data(**kwargs)
    context['title'] = 'Final questions'
    context['heading'] = 'List of final Questions'
    return context


Q5-(1.5 marks) -- Define a class-based view called QuestionDetailView that uses DetailView: 

from django.views.generic import DetailView
class QuestionDetailView(DetailView):
    model = Question

    template_name = 'Evaluation/question_detail.html'


  def get_context_data(self, **kwargs):
    context = super().get_context_data(**kwargs)
    context['title'] = 'Question Detail'
    return context

