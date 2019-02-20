package messaging.requestreply;

public enum QueueTypes {
	LoanRequest{
		public String toString(){
			return "LoanRequest";
		}
	},
	LoanReply{
		public String toString(){
			return "LoanReply";
		}
	},
	BankRequest{
		public String toString(){
			return "BankRequest";
		}
	},
	BankReply{
		public String toString(){
			return "BankReply";
		}
	}
}
