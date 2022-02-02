import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class line {
    private Integer lineNumber;
    private String line;

    public line(int lineNumber, String line)
    {
        this.lineNumber = lineNumber;
        this.line = line;
    }

    public Integer getLineNumber()
    { return lineNumber; }

    public String getLine()
    {
        List<String> splitLine = Arrays.asList(line.split(" "));
        List<String> formatted = new ArrayList<>();
        for(int i = 0; i < splitLine.size(); i++)
        {
            if(!splitLine.get(i).isEmpty())
            {
                formatted.add(splitLine.get(i));
            }
        }
        return String.join(" ", formatted);
    }

}
