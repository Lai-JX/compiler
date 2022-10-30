package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FilePathConfig;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;      // 符号表
    private char[] buffer;                      // 输入缓冲
    private List<Token> tokens = new ArrayList<Token>();                 // 词法单元集合

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) throws IOException {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        File file = new File(path);
        InputStream input = new FileInputStream(file);
        byte[] b = new byte[(int)file.length()];
        input.read(b,0,b.length);
        String str = new String(b);
        // 剔除空格、换行等
        str = str.replaceAll("\s|\t|\r|\n| +", "");

        this.buffer = str.toCharArray();
//        System.out.println(Arrays.toString(buffer));
//        System.out.println(buffer[27]);
        return;
//        throw new NotImplementedException();
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // TODO: 自动机实现的词法分析过程
        for(int i=0; i<buffer.length; ){
            int begin = i;
            String word=null;

            char ch = buffer[i];
            if(isLetter(ch)){           // 是否为字母
                boolean flag = false;   // 用于标志是否为保留字

                // 判别保留字
                while (isLetter(ch)){
                    ch = buffer[++i];
                    word = new String(Arrays.copyOfRange(buffer, begin, i));
                    switch (word){
                        case "int" -> {
                                tokens.add(Token.simple(TokenKind.fromString("int")));
                                flag = true;
                            }
                        case "return" -> {
                                tokens.add(Token.simple(TokenKind.fromString("return")));
                                flag = true;
                            }
                        default -> {}
                    }
                    if(flag){       // 找到保留字，直接跳出循环
                        break;
                    }
                }

                // 没找到保留字，则说明该词为变量名
                if(!flag){
                    while (isDigit(ch) || isLetter(ch) || ch=='_'){
                        ch = buffer[++i];
                    }
                    word = new String(Arrays.copyOfRange(buffer, begin, i));
                    if(!symbolTable.has(word)){
                        symbolTable.add(word);
                    }
                    tokens.add(Token.normal(TokenKind.fromString("id"),word));
                }


            }else if(isDigit(ch)){      // 是否为数字
                while (isDigit(ch)){
                    ch = buffer[++i];
                }
                word = new String(Arrays.copyOfRange(buffer, begin, i));
                tokens.add(Token.normal(TokenKind.fromString("IntConst"),word));

            }else{
                switch (ch){
                    case '=' -> tokens.add(Token.simple(TokenKind.fromString("=")));
                    case ',' -> tokens.add(Token.simple(TokenKind.fromString(",")));
                    case '+' -> tokens.add(Token.simple(TokenKind.fromString("+")));
                    case '-' -> tokens.add(Token.simple(TokenKind.fromString("-")));
                    case '*' -> tokens.add(Token.simple(TokenKind.fromString("*")));
                    case '/' -> tokens.add(Token.simple(TokenKind.fromString("/")));
                    case '(' -> tokens.add(Token.simple(TokenKind.fromString("(")));
                    case ')' -> tokens.add(Token.simple(TokenKind.fromString(")")));
                    case ';' -> tokens.add(Token.simple(TokenKind.fromString("Semicolon")));
                    default -> throw new RuntimeException("未定义字符");
                }
                i++;
            }
        }
        tokens.add(Token.eof());
        return;
//        throw new NotImplementedException();
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        return tokens;
//        throw new NotImplementedException();
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}
