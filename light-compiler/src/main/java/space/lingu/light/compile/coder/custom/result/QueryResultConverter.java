package space.lingu.light.compile.coder.custom.result;

import space.lingu.light.compile.coder.GenerateCodeBlock;
import space.lingu.light.compile.coder.custom.QueryContext;

/**
 * @author RollW
 */
public interface QueryResultConverter {
    void convert(QueryContext queryContext, GenerateCodeBlock block);
}
