package da2.message;

public class TextMessage extends Message {
	
	private static final long serialVersionUID = 1259730982243753379L;
	String textContent;
	
	public TextMessage(String content)
	{
		this.textContent = content;
	}
	
	public String getTextContent()
	{
		return textContent;
	}
	
	@Override
	public boolean equals(Object o){
		boolean is = false;
		if(o instanceof TextMessage){
			TextMessage t = (TextMessage) o;
			is = textContent.equals(t.getTextContent());
		}
		return is;
	}

}
