package io.hypercell.core.expression;

import org.antlr.v4.runtime.tree.ParseTree;
import io.hypercell.formula.HyperCellExpressionParser.ItemContext;
import io.hypercell.formula.HyperCellExpressionParser.OffsetContext;
import io.hypercell.api.CellAddress;
import io.hypercell.api.CellValue;
import io.hypercell.core.grid.FormulaError;
import io.hypercell.core.grid.MemCell;
import io.hypercell.core.grid.MemSheet;
import io.hypercell.core.grid.MemWorkbook;

public class Identifier extends AbstractExpression
{
    private String name;
    private CellAddress address;
    private int offset;
    private MemSheet parentSheet;
    private Double nullValue;
    private boolean isTableCount;

    public Identifier(String name)
    {
        this.name = name;
        this.isTableCount = true;
    }

    public Identifier(ParseTree tree, MemSheet parentSheet)
    {
        if (tree instanceof OffsetContext)
        {
            this.name = tree.getChild(1).getText().trim();
            this.offset = Integer.parseInt(tree.getChild(3).getText());
        } else if (tree instanceof ItemContext)
        {
            this.name = tree.getText().trim();
            if (name.charAt(0) == '\'' && name.charAt(name.length() - 1) == '\'')
            {
                name = name.substring(1, name.length() - 1);
            }
            address = CellAddress.getCellAddress(name);
            if (address != null && address.sheetName != null)
            {
                this.parentSheet = parentSheet.getWorkbook().getSheet(address.sheetName);
            }
        }
        if (this.parentSheet == null)
        {
            this.parentSheet = parentSheet;
        }
    }

    public Identifier(MemWorkbook workbook, CellAddress address)
    {
        this.parentSheet = workbook.getSheet(address.sheetName);
        this.address = address;
    }

    public Identifier(MemSheet parentSheet, CellAddress address)
    {
        this.parentSheet = parentSheet;
        this.address = address;
    }

    @Override
    public CellValue evaluate()
    {
        if (parentSheet != null)
        {
            if (address == null)
            {
                return null;
            }
            MemCell mc = parentSheet.getCellAt(address);
            if (mc == null)
            {
                return null;
            }
            mc.calculate();
            return mc;
        }
        return null;
    }

    @Override
    public String getExcelFormula()
    {
        if (address == null)
        {
            return "@NA";
        }
        return getSheetColumn(address.column + offset) + Integer.toString(address.row + 1);
    }

    private String getSheetColumn(int colNumber)
    {
        StringBuilder sb = new StringBuilder();
        int curValue = colNumber;
        do
        {
            int c = curValue % 26;
            sb.insert(0, (char) ((int) 'A' + c));
            curValue = (curValue / 26) - 1;
        } while (curValue >= 0);
        return sb.toString();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        if (name == null)
        {
            return address.toString();
        }
        return name;
    }

    public int getRow()
    {
        if (address == null)
            return 0;
        return address.row;
    }

    public void setRow(int row)
    {
        if (this.address == null)
        {
            this.address = new CellAddress(row, 0);
        } else
        {
            this.address.row = row;
        }
    }

    public int getColumn()
    {
        return address.column;
    }

    public void setColumn(int column)
    {
        if (this.address == null)
        {
            this.address = new CellAddress(0, column);
        } else
        {
            this.address.column = column;
        }
    }

    public void setNullValue(Double nullValue)
    {
        this.nullValue = nullValue;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = Math.max(offset, 0);
    }

    public MemSheet getSheet()
    {
        return parentSheet;
    }

    public void setSheet(MemSheet sheet)
    {
        this.parentSheet = sheet;
    }

    public String getSheetName()
    {
        return address.sheetName;
    }

    public CellAddress getAddress()
    {
        return address;
    }

    public void setAddress(CellAddress address)
    {
        this.address = address;
    }

    public boolean isTableCount()
    {
        return isTableCount;
    }
}
