
public class test {

	public test() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String test = "/libvirt/instance-00000001/cpu/vcpu/0/cputime";
		if (test.matches("^\\/libvirt\\/([0-9]|[a-z]|_|\\-)*\\/cpu\\/vcpu\\/(0|[1-9][0-9]*)\\/cputime$")) {
			System.out.println("Yes");
		} else System.out.println("No");	
	}
}

