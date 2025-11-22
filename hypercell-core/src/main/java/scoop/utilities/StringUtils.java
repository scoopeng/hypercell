package scoop.utilities;

public class StringUtils
{
    public static String getParseableFormulaFromLLMFormula(String formula)
    {
        StringBuilder result = new StringBuilder();
        boolean inQuotes = false;
        // Replace single quotes with double quotes unless inside of a double quote
        for (int i = 0; i < formula.length(); i++)
        {
            char c = formula.charAt(i);
            if (i == 0 && c == '=')
                continue;
            if (c == '\"')
            {
                inQuotes = !inQuotes;
                result.append(c);
            } else if (formula.charAt(i) == '\'')
            {
                if (!inQuotes)
                {
                    result.append("\"");
                } else
                {
                    result.append(c);
                }
            } else
            {
                result.append(c);
            }
        }
        return result.toString().replaceAll("[|]", "'");
    }

    public static String makeIntoColumnName(String proposedName)
    {
        if (proposedName == null)
            return null;
        // Strip illegal characters
        proposedName = proposedName.replaceAll("[%]", "pct");
        proposedName = proposedName.replaceAll("\\s[&]\\s", " and ").replaceAll("[&]", " and ");
        String result = proposedName.replaceAll("([^a-zA-Z_0-9\\s[-]/]|[\\n\\r\\t])", "").trim();

        // Prevent empty column names (e.g., from headers like "###" or "!!!")
        if (result.isEmpty())
        {
            return "Column";
        }

        return result;
    }

    public static String stripQuotes(String s)
    {
        if (s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"')
            return (s.substring(1, s.length() - 1));
        return s;
    }

    public static String stripFileExtension(String fileName)
    {
        if (fileName == null)
            return null;
        int x = fileName.lastIndexOf('.');
        if (x < 0)
            return fileName;
        return fileName.substring(0, x - 1);
    }

    public static String getIDName(String proposedName)
    {
        // Strip illegal characters
        return proposedName.replaceAll("[^a-zA-Z_0-9]", "_").trim();
    }

    public static boolean isPluralName(String name)
    {
        return name.toLowerCase().endsWith("s");
    }

    // Indicators that this is a measure
    private static final String[] measureWords = new String[]{"sum", "total", "avg", "count"};

    public static boolean containsMeasureWord(String s)
    {
        String[] parts = s.toLowerCase().split("[:|\\s_]");
        for (String part : parts)
        {
            for (String mword : measureWords)
            {
                if (part.contains(mword))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Escape special characters in a string so that it can be bulk loaded
     */
    public static String escape(String s)
    {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Escape special characters in a string so that it can be used in a SQL statement
     */
    public static String escapeSQL(String s)
    {
        return s.replace("'", "\\'").replace("\"", "\\\"");
    }

    public static String removeNumeric(String s)
    {
        return s.replaceAll("\\d", "");
    }

    public static boolean equalsWithoutNumeric(String a, String b)
    {
        return removeNumeric(a).equals(removeNumeric(b));
    }

    public static String properCase(String s)
    {
        StringBuilder sb = new StringBuilder();
        if (!s.isEmpty())
        {
            sb.append(Character.toUpperCase(s.charAt(0)));
        }
        for (int i = 1; i < s.length(); i++)
        {
            sb.append(Character.toLowerCase(s.charAt(i)));
        }
        return sb.toString();
    }

    public static String quoteIfHasSpaces(String s)
    {
        if (s.indexOf(' ') >= 0)
        {
            return "'" + s + "'";
        }
        return s;
    }

    public static String unEncodeHTMLCellAddressString(String s)
    {
        // Replace any html that has snuck into the text run and remove any spans that might be splitting it because Powerpoint split it into two text runs
        return s.replaceAll("(&#39;|[`]|&apos;|&rsquo;|&lsquo;|&#8216;|&#8217;)", "'").
                replaceAll("(&nbsp;| )", " ").replaceAll(
                        "</span><span style=\"font-size:([a-zA-Z.:;-]|\\d|,|\\()+\\);\">", "");
    }

    private static final char[] charactersToReplace = new char[]{'\\', '{', '}'};

    public static String URLEncode(String url)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < url.length(); i++)
        {
            char c = url.charAt(i);
            boolean found = false;
            for (char value : charactersToReplace)
            {
                if (c == value)
                {
                    result.append("%").append(Integer.toHexString(c));
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                result.append(c);
            }
        }
        return result.toString();
    }
}
