
public class test {

	public test() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String test = "/intel/psutil/net/qvb9bff2afa-ec/bytes_sent";
		if (test.matches("^\\/intel\\/psutil\\/net\\/([0-9]|[a-z]|_|\\.|-)*\\/(bytes_recv|bytes_sent|dropin|dropout|errin|errout|packets_recv|packets_sent)$")) {
			System.out.println("Yes");
		} else System.out.println("No");	
	}
}

