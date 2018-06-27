package CyclicBarrier;

public class BillServiceImpl implements BillService {

	/**
	 *代码，按省代码分类，各省数据库独立。   
	 */
	@Override
	public void bill(String code) {
		// TODO Auto-generated method stub
		System.out.println(code+" ： 计算中。。。。。。。  ");
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

}
