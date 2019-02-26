package loanbroker;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import model.bank.*;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

public class LoanBrokerFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
    private JList<JListLine> list;

    private BankAppGateway bankGateway;
    private LoanClientGateway clientGateway;
    private List<String> bank;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    LoanBrokerFrame frame = new LoanBrokerFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public LoanBrokerFrame() {


        clientGateway = new LoanClientGateway() {
            @Override
            void onLoanRequestArrived(LoanRequest loanreq) throws EvaluationException {
                add(loanreq);
                BankInterestRequest bankreq = new BankInterestRequest(loanreq.getAmount(), loanreq.getTime());
                bank = getBanks(bankreq);
                System.out.println(bank.toString());

                System.out.println("Broker sending BankInterestRequest to bank: " + bankreq);
                add(loanreq, bankreq);

                for(String s :bank) {
                    bankGateway = bankGateway = new BankAppGateway(s) {
                        @Override
                        public void onBankReplyArrived(BankInterestRequest request, BankInterestReply reply) {
                            add(request, reply);
                            System.out.println("Broker sending LoanReply to client: " + reply.toString());
                            clientGateway.sendLoanReply(getRequestReply(request).getLoanRequest(), new LoanReply(reply.getInterest(), reply.getQuoteId()));
                        }
                    };
                    bankGateway.sendBankRequest(bankreq);
                }

            }
        };
//        bankGateway = new BankAppGateway() {
//            @Override
//            public void onBankReplyArrived(BankInterestRequest request, BankInterestReply reply) {
//                add(request, reply);
//                System.out.println("Broker sending LoanReply to client: " + reply.toString());
//                clientGateway.sendLoanReply(getRequestReply(request).getLoanRequest(), new LoanReply(reply.getInterest(), reply.getQuoteId()));
//            }
//        };
        setTitle("Loan Broker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{46, 31, 86, 30, 89, 0};
        gbl_contentPane.rowHeights = new int[]{233, 23, 0};
        gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridwidth = 7;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 0;
        contentPane.add(scrollPane, gbc_scrollPane);

        list = new JList<JListLine>(listModel);
        scrollPane.setViewportView(list);
    }

    public void setBankGateway(BankAppGateway gateway){
        this.bankGateway = gateway;
    }
    private JListLine getRequestReply(LoanRequest request) {

        for (int i = 0; i < listModel.getSize(); i++) {
            JListLine rr = listModel.get(i);
            if (rr.getLoanRequest() == request) {
                return rr;
            }
        }
        return null;
    }

    private JListLine getRequestReply(BankInterestRequest request) {

        for (int i = 0; i < listModel.getSize(); i++) {
            JListLine rr = listModel.get(i);
            if (rr.getBankRequest() == request) {
                return rr;
            }
        }
        return null;
    }

    public void add(LoanRequest loanRequest) {
        listModel.addElement(new JListLine(loanRequest));
    }

    public void add(LoanRequest loanRequest, BankInterestRequest bankRequest) {
        JListLine rr = getRequestReply(loanRequest);
        if (rr != null && bankRequest != null) {
            rr.setBankRequest(bankRequest);
            list.repaint();
        }
    }

    public void add(BankInterestRequest request, BankInterestReply reply) {
        JListLine rr = getRequestReply(request);
        if (rr != null && reply != null) {
            rr.setBankReply(reply);
            list.repaint();
        }
    }

    public List<String> getBanks(BankInterestRequest bankRequest) throws EvaluationException {
        String ING       = "#{amount} <= 100000 && #{time} <= 10";
        String ABN_AMRO  = "#{amount} >= 200000 && #{amount} <= 300000  && #{time} <= 20";
        String RABO_BANK = "#{amount} <= 250000 && #{time} <= 15";

        Evaluator evaluator = new Evaluator(); // for evaluation of bank rules
        BankInterestRequest request = bankRequest;

        // set values of variables amount and time
        evaluator.putVariable("amount", Integer.toString(request.getAmount()));
        evaluator.putVariable("time", Integer.toString(request.getTime()));

        String result = evaluator.evaluate(ING); // evaluate ING rule
        boolean ingRule = result.equals("1.0"); // 1.0 means TRUE, otherwise it is FALSE

        String result1 = evaluator.evaluate(ABN_AMRO); // evaluate ABN Amro rule
        boolean abnRule = result1.equals("1.0"); // 1.0 means TRUE, otherwise it is FALSE

        String result2 = evaluator.evaluate(RABO_BANK); // evaluate RaboBank rule
        boolean raboRule = result2.equals("1.0"); // 1.0 means TRUE, otherwise it is FALSE

        List<String> banks = new ArrayList<>();
        if(abnRule){
            banks.add("ABN AMRO");
        }
        if(ingRule){
            banks.add("ING");
        }
        if(raboRule){
            banks.add("RABO");
        }
         return banks;
    }


}