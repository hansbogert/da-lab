package da;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Process extends UnicastRemoteObject implements IHandleRMI{
	ArrayList<Message> buffer;

	public Process() throws RemoteException{
		
	}
	
	void register(String ip, String processName) {
		try{
			// create a new service named myMessage
			LocateRegistry.getRegistry(ip, 1099);
			
            Registry myRegistry = LocateRegistry.getRegistry(ip, 1099);
			
            myRegistry.rebind(processName, this);
            
            IHandleRMI otherprocess;
            if(processName.equals("1"))
            {/*
                 otherprocess = (Process) myRegistry.lookup("2");
                 Message m= new Message();
                 m.payload = "Hello I am Process" + processName;
                 otherprocess.receive(m); */
            }
            else if(processName.equals("2"))
            {
            	otherprocess = (IHandleRMI) myRegistry.lookup("1");
                Message m= new Message();
                m.payload = "Hello I am Process" + processName;
                otherprocess.receive(m);
            }
            else
            {
            	
            }


            
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("system is ready, Process" + processName);
	}
	
	
	
	public void receive(Message m) throws RemoteException
	{
		System.out.println(m.payload);
	}
}
