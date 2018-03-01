// StyleCheckType Scanner
// StyleCheckType HashMap
// StyleCheckType ArrayList
// StyleCheckType Stack

// 開發環境 : Eclipse
// 作業系統 : Windows 7
// 設計模式 : Composite Pattern, Interpreter Pattern
// 作法 : 將每一條文法都想像成一個節點，每一條文法底下的子文法都視為子節點，...
// Parser : 利用 HeadOf() 來判斷要走哪一條子文法，並且 new 出相對印的文法實體( instance )，再呼叫子文法的 Parse()...，最終會長出一棵樹
// 在我沒將 userInput 指向 null 之前，此樹會一直存在
// Execute : 依照每一條文法的意義作相對應的計算，與 parse 很類似的地方是，也是不斷地呼叫子文法的 execute() ,順著樹的形狀算出數值，並將數值以 Constant 封裝後回傳

package Cacocacoon ;

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.HashMap ;
import java.util.Scanner ;
import java.util.Stack ;
import java.util.Vector ;

interface Node {
  // public void Parse() throws Exception ;
  // public void Execute() throws Exception ;
} // interface Node

class User_Input implements Node {
  Definition m_definition = null ;
  Statement m_statement = null ;
  
  public void Parse() throws Exception {
    if ( Definition.HeadOf( G.s_Context.PeekToken() ) ) {
      m_definition = new Definition() ;
      m_definition.Parse() ;
    } // if
    else if ( Statement.HeadOf( G.s_Context.PeekToken() ) ) {
      m_statement = new Statement() ;
      m_statement.Parse() ;
    } // else if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()

  public void Execute() throws Exception {
    if ( m_statement != null ) {
      m_statement.Execute() ;
      if ( G.s_running )
        System.out.println( "Statement executed ..." ) ;
    } // if
  } // Execute()
  
  public static boolean HeadOf( String token ) {
    return ( Definition.HeadOf( token ) || Statement.HeadOf( token ) ) ;
  } // HeadOf()
} // class User_Input implements Node

class Definition implements Node {
  Type_specifier m_typeSpecifier = null ;
  Identifier m_identifier = null ;
  Function_definition_without_ID m_functionDefinitionWithoutID = null ;
  Function_definition_or_declarators m_funtionDefinitionOrDeclarators = null ;
  
  public void Parse() throws Exception {
    if ( G.s_Context.PeekToken().equals( "void" ) ) {
      
      String id = null ;
      
      G.s_Context.GetToken() ; // skip "void"
      if ( Identifier.HeadOf( G.s_Context.PeekToken() ) ) {
        m_identifier = new Identifier() ;
        m_identifier.Parse() ;
        id = m_identifier.Execute() ; // 回傳變數
        if ( Function_definition_without_ID.HeadOf( G.s_Context.PeekToken() ) ) {
          m_functionDefinitionWithoutID = new Function_definition_without_ID() ;
          m_functionDefinitionWithoutID.Parse( null, id ) ; // 將變數傳入此帕斯，以便做宣告。
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // if
    else if ( Type_specifier.HeadOf( G.s_Context.PeekToken() ) ) {
      String type = null ;
      String id = null ;
      
      m_typeSpecifier = new Type_specifier() ;
      m_typeSpecifier.Parse() ;
      type = m_typeSpecifier.Execute() ;
      if ( Identifier.HeadOf( G.s_Context.PeekToken() ) ) {
        m_identifier = new Identifier() ;
        m_identifier.Parse() ;
        id = m_identifier.Execute() ;
        if ( Function_definition_or_declarators.HeadOf( G.s_Context.PeekToken() ) ) {
          m_funtionDefinitionOrDeclarators = new Function_definition_or_declarators() ;
          m_funtionDefinitionOrDeclarators.Parse( type, id ) ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()

  public void Execute() throws Exception {
    
  } // Execute()
  
  public static boolean HeadOf( String token ) {
    return ( token.equals( "void" ) || Type_specifier.HeadOf( token ) ) ;
  } // HeadOf()
} // class Definition implements Node

class Identifier implements Node {
  String m_id = null ;
  public void Parse() throws Exception {
    m_id = G.s_Context.GetToken() ;
    if ( !HeadOf( m_id ) )
      Error.Handle( m_id, Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public String Execute() throws Exception {
    return m_id ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    if ( ReserveWord.Check( token ) )
      return false ;
    else
      return Character.isLetter( token.charAt( 0 ) ) ;
  } // HeadOf()

  public static boolean HasDefined( Identifier identifier ) throws Exception {
    if ( FunctionTable.s_allFunction.containsKey( identifier.Execute() ) ||
         FunctionTable.IsSystemSupportFunction( identifier.Execute() ) )
      return true ;
    else if ( VariableTable.HasDefined( identifier ) )
      return true ;
    else
      return false ;
  } // HasDefined()
} // class Identifier implements Node

class Type_specifier implements Node {
  public final static int INT = 0 ;
  public final static int FLOAT = 1 ;
  public final static int CHAR = 2 ;
  public final static int BOOL = 3 ;
  public final static int STRING = 4 ;
  
  String m_typeSpecifier = null ;
  
  // public static Vector<String> s_tokenCollections = new Vector<String>() ;
  // public static boolean boolExpressionChecker = false ;
  
  public void Parse() throws Exception {
    m_typeSpecifier = G.s_Context.GetToken() ;
  } // Parse()
 
  public String Execute() throws Exception {
    return m_typeSpecifier ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return token.equals( "int" ) || token.equals( "char" ) || token.equals( "float" ) ||
           token.equals( "string" ) || token.equals( "bool" ) ;
  } // HeadOf()
} // class Type_specifier implements Node

class Function_definition_or_declarators implements Node {
  Function_definition_without_ID m_functionDefinitionWithoutID = null ;
  Rest_of_declarators m_restOfDeclarators = null ;
  
  public void Parse( String type, String id ) throws Exception {
    if ( Function_definition_without_ID.HeadOf( G.s_Context.PeekToken() ) ) {
      m_functionDefinitionWithoutID = new Function_definition_without_ID() ;
      m_functionDefinitionWithoutID.Parse( type, id ) ;
    } // if
    else if ( Rest_of_declarators.HeadOf( G.s_Context.PeekToken() ) ) {
      m_restOfDeclarators = new Rest_of_declarators() ;
      m_restOfDeclarators.Parse( type, id ) ;
      m_restOfDeclarators.Execute() ;
    } // else if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public static boolean HeadOf( String token ) {
    return ( Function_definition_without_ID.HeadOf( token ) || Rest_of_declarators.HeadOf( token ) ) ;
  } // HeadOf()
} // class Function_definition_or_declarators implements Node

class Constant implements Node {
  String m_value = null ;
  int m_type = -1 ;
  
  public Constant( String value ) {
    SetValue( value ) ;
  } // Constant()
  
  public Constant() {
    
  } // Constant()
  
  public String GetType() {
    if ( m_type == Type_specifier.STRING )
      return "string" ;
    else if ( m_type == Type_specifier.CHAR )
      return "char" ;
    else if ( m_type == Type_specifier.FLOAT )
      return "float" ;
    else if ( m_type == Type_specifier.BOOL )
      return "bool" ;
    else
      return "int" ;
  } // GetType()
  
  public String GetValue() {
    return m_value ;
  } // GetValue()
  
  private void SetValue( String value ) {
    if ( value.charAt( 0 ) == '"' )
      m_type = Type_specifier.STRING ;
    else if ( value.charAt( 0 ) == '\'' )
      m_type = Type_specifier.CHAR ;
    else if ( value.contains( "." ) )
      m_type = Type_specifier.FLOAT ;
    else if ( value.equals( "true" ) || value.equals( "false" ) )
      m_type = Type_specifier.BOOL ;
    else
      m_type = Type_specifier.INT ;
    
    if ( value.charAt( 0 ) == '"' || value.charAt( 0 ) == '\'' )
      m_value = value.substring( 1, value.length() - 1 ) ;
    else
      m_value = value ;
  } // SetValue()
  
  public Constant Copy() {
    Constant constant = new Constant() ;
    constant.m_value = new String( this.m_value ) ;
    constant.m_type = this.m_type ;
    return constant ;
  } // Copy()

  public void Parse() throws Exception {
    SetValue( G.s_Context.GetToken() );
  } // Parse()
  
  public String Execute() throws Exception {
    return m_value ;
  } // Execute()
  
  public static boolean HeadOf( String token ) {
    if ( token.equals( "true" ) || token.equals( "false" )  )
      return true ;
    else if ( token.charAt( 0 ) == '.' || Character.isDigit( token.charAt( 0 ) ) )
      return true ;
    else if ( token.charAt( 0 ) == '\'' || token.charAt( 0 ) == '"' )
      return true ;
    else
      return false ;
  } // HeadOf()
} // class Constant implements Node
 
class Rest_of_declarators implements Node {
  
  Constant m_firstConstant = null ;
  String m_firstId = null ;
  Identifier[] m_ids = new Identifier[20] ;
  Constant[] m_cnsts = new Constant[20] ;
  String m_type = null ;

  public void Parse( String t, String id ) throws Exception {
    m_type = t ;
    m_firstId = id ;
    if ( G.s_Context.PeekToken().equals( "[" ) ) {
      G.s_Context.GetToken() ; // skip "["
      if ( Constant.HeadOf( G.s_Context.PeekToken() ) ) {
        m_firstConstant = new Constant() ;
        m_firstConstant.Parse() ;
        if ( G.s_Context.PeekToken().equals( "]" ) )
          G.s_Context.GetToken() ;
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // if
    
    if ( G.s_Context.PeekToken().equals( "," ) ) {
      // i 用來記錄趴司到第幾個變數，且用來把變數存在相對應的陣列位置
      for ( int i = 0; G.s_Context.PeekToken().equals( "," ) ; i++ ) {
        G.s_Context.GetToken() ; // skip ","
        if ( Identifier.HeadOf( G.s_Context.PeekToken() ) ) {
          Identifier identifier = new Identifier() ;
          identifier.Parse() ;
          m_ids[i] = identifier ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

        if ( G.s_Context.PeekToken().equals( "[" ) ) {
          G.s_Context.GetToken() ; // skip "["
          if ( Constant.HeadOf( G.s_Context.PeekToken() ) ) {
            Constant cnst = new Constant() ;
            cnst.Parse() ;
            m_cnsts[i] = cnst ;
          }  // if
          else
            Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
          if ( G.s_Context.PeekToken().equals( "]" ) )
            G.s_Context.GetToken() ;
          else
            Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
        } // if
      } // for
    } // if

    if ( G.s_Context.PeekToken().equals( ";" ) )
      G.s_Context.GetToken() ;
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
  
  public HashMap<String, Variable> Execute() throws Exception {
    if ( G.s_scope != G.GLOBAL_SCOPE ) { // 若是區域變數，就要把宣告的變數回傳存在compound statement 裡面
      HashMap<String, Variable> variables = new HashMap<String, Variable>() ;
      if ( m_firstConstant != null ) // is array
        variables.put( m_firstId, new Variable( m_firstId, null, m_type, m_firstConstant.Execute() ) ) ;
      else
        variables.put( m_firstId, new Variable( m_firstId, null, m_type, null ) ) ;
      for ( int i = 0 ; i < 20 && m_ids[i] != null ; i++ ) {
        if ( m_ids[i] != null && m_cnsts[i] != null ) // is array
          variables.put( m_ids[i].Execute(),
                         new Variable( m_ids[i].Execute(), null, m_type, m_cnsts[i].Execute() ) ) ;
        else if ( m_ids[i] != null && m_cnsts[i] == null ) // not array
          variables.put( m_ids[i].Execute(),
                         new Variable( m_ids[i].Execute(), null, m_type, null ) ) ;
      } // for

      return variables ;
    } // if
    else {
      // if ( G.s_variableTables.isEmpty() )
      //   G.s_variableTables.add( G.GLOBAL_SCOPE, new VariableTable() ) ;
      // VariableTable variableTable = G.s_variableTables.get( G.GLOBAL_SCOPE ) ;
      VariableTable variableTable = G.s_globalVariableTable ;
      
      if ( variableTable.m_variables.containsKey( m_firstId ) )
        System.out.println( "New definition of " + m_firstId + " entered ..." ) ;
      else
        System.out.println( "Definition of " + m_firstId + " entered ..." ) ;
      if ( m_firstConstant != null ) { // is array
        String firstConstant = m_firstConstant.Execute() ;
        variableTable.m_variables.put( m_firstId, new Variable( m_firstId, null, m_type, firstConstant ) ) ;
      } // if
      else
        variableTable.m_variables.put( m_firstId, new Variable( m_firstId, null, m_type, null ) ) ;
      
      for ( int i = 0 ; i < 20 && m_ids[i] != null ; i++ ) {
        String id = m_ids[i].Execute() ;
        if ( variableTable.m_variables.containsKey( id ) )
          System.out.println( "New definition of " + id + " entered ..." ) ;
        else
          System.out.println( "Definition of " + id + " entered ..." ) ;
        if ( m_cnsts[i] != null ) // is array
          variableTable.m_variables.put( id, new Variable( id, null, m_type, m_cnsts[i].Execute() ) ) ;
        else // not array
          variableTable.m_variables.put( id, new Variable( id, null, m_type, null ) ) ;
      } // for
      // G.s_variableTables.add( G.GLOBAL_SCOPE, variableTable ) ;
      
      return null ;
    } // else
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return token.equals( "[" ) || token.equals( "," ) || token.equals( ";" ) ;
  } // HeadOf()
} // class Rest_of_declarators implements Node

// Function_definition_without_ID 這邊是做儲存function的地方
class Function_definition_without_ID implements Node {
  Formal_parameter_list m_formalParameterList = null ;
  Compound_statement m_compoundStatement = null ;
  Argument[] m_args = null ;

  public void Parse( String type, String id ) throws Exception {
    if ( G.s_Context.GetToken().equals( "(" ) ) {
      // String token = G.s_Context.GetToken() ;
      if ( G.s_Context.PeekToken().equals( "void" ) ) { // m_formalParameterList 仍然是  null
        G.s_Context.GetToken() ; // skip "void"
        G.s_ActivationRecordStack.push( new ActivationRecord( id, null ) ) ; // 先 push 到 stack 裡，以便檢查
      } // if
      else if ( Formal_parameter_list.HeadOf( G.s_Context.PeekToken() ) ) {
        m_formalParameterList = new Formal_parameter_list() ;
        m_formalParameterList.Parse() ;
        m_args = m_formalParameterList.Execute() ;
        G.s_ActivationRecordStack.push( new ActivationRecord( id, m_args ) ) ; // 先 push 到 stack 裡，以便檢查
      } // else if
      
      if ( G.s_Context.PeekToken().equals( ")" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    
    if ( Compound_statement.HeadOf( G.s_Context.PeekToken() ) ) {
      m_compoundStatement = new Compound_statement() ;
      try {
        G.s_scope++ ;
        FunctionTable.s_startCollectToken = true ;
        FunctionTable.s_tokenCollections.add( "{" ) ; // 這邊很爛，因為"{"已經先被peekToken讀過了，所以要補進去
        m_compoundStatement.Parse() ;
        FunctionTable.s_allFunctionStmt.put( id, FunctionTable.s_tokenCollections ) ;
        G.s_scope-- ;
      } // try
      finally {
        FunctionTable.s_startCollectToken = false ;
        FunctionTable.s_tokenCollections = new Vector<String>() ;
      } // finally
      
    } // if

    Execute( type, id ) ; // 立即儲存。
    G.s_ActivationRecordStack.pop() ; // 離開方玄定義之前，要先 pop 掉 ActivationRecord
  } // Parse()
 
  public void Execute( String type, String id ) throws Exception {
    if ( FunctionTable.s_allFunction.containsKey( id ) )
      System.out.println( "New definition of " + id + "() entered ..." ) ;
    else
      System.out.println( "Definition of " + id + "() entered ..." ) ;
    FunctionTable.s_allFunction.put( id, new Function( id, m_args, type, m_compoundStatement ) ) ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return token.equals( "(" ) ;
  } // HeadOf()
} // class Function_definition_without_ID implements Node
 
class Formal_parameter_list implements Node {
  Argument[] m_args = null ;
  public void Parse() throws Exception {
    m_args = new Argument[20] ;
    int count = 0 ;
    
    String t = null ;
    boolean ref = false ;
    String identifier  = null ;
    String cnst = null ;
    if ( Type_specifier.HeadOf( G.s_Context.PeekToken() ) ) {
      Type_specifier type = new Type_specifier() ;
      type.Parse() ;
      t = type.Execute() ;
      if ( G.s_Context.PeekToken().equals( "&" ) ) {
        ref = true ;
        G.s_Context.GetToken() ; // skip "&"
      } // if

      if ( Identifier.HeadOf( G.s_Context.PeekToken() ) ) {
        Identifier id = new Identifier() ;
        id.Parse() ;
        identifier = id.Execute() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      if ( G.s_Context.PeekToken().equals( "[" ) ) {
        G.s_Context.GetToken() ; // skip "["
        if ( Constant.HeadOf( G.s_Context.PeekToken() ) ) {
          Constant constant = new Constant() ;
          constant.Parse() ;
          cnst = constant.Execute() ;
          if ( G.s_Context.PeekToken().equals( "]" ) )
            G.s_Context.GetToken() ;
          else
            Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // if

      m_args[count++] = new Argument( t, ref, identifier, cnst ) ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    
    while ( G.s_Context.PeekToken().equals( "," ) ) {
      G.s_Context.GetToken() ; // skip ","
      t = null ;
      ref = false ;
      identifier  = null ;
      cnst = null ;
      
      if ( Type_specifier.HeadOf( G.s_Context.PeekToken() ) ) {
        Type_specifier type = new Type_specifier() ;
        type.Parse() ;
        t = type.Execute() ;
        if ( G.s_Context.PeekToken().equals( "&" ) ) {
          ref = true ;
          G.s_Context.GetToken() ; // skip "&"
        } // if

        if ( Identifier.HeadOf( G.s_Context.PeekToken() ) ) {
          Identifier id = new Identifier() ;
          id.Parse() ;
          identifier = id.Execute() ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
        if ( G.s_Context.PeekToken().equals( "[" ) ) {
          G.s_Context.GetToken() ; // skip "["
          if ( Constant.HeadOf( G.s_Context.PeekToken() ) ) {
            Constant constant = new Constant() ;
            constant.Parse() ;
            cnst = constant.Execute() ;
            if ( G.s_Context.PeekToken().equals( "]" ) )
              G.s_Context.GetToken() ;
            else
              Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
          } // if
          else
            Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
        } // if

        m_args[count++] = new Argument( t, ref, identifier, cnst ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  
  } // Parse()
 
  public Argument[] Execute() throws Exception {
    return m_args ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Type_specifier.HeadOf( token ) ;
  } // HeadOf()
} // class Formal_parameter_list implements Node
 
class Compound_statement implements Node {
  // 每一個 compound statement 都各自儲存自己的變數，當趴司完declaration要趴司statement或是要執行statement之前，
  // 要把vars放到G.s_variable[G.scope]裡面，這樣才能檢查是否定義以及拿來計算。
  Vector<Statement> m_statements = null ;
  VariableTable m_vars ;
  
  public void Parse() throws Exception {
    if ( G.s_Context.PeekToken().equals( "{" ) )
      G.s_Context.GetToken() ;
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    while ( Declaration.HeadOf( G.s_Context.PeekToken() ) ) {
      Declaration declaration = new Declaration() ;
      declaration.Parse() ;
      if ( m_vars == null )
        m_vars = new VariableTable() ;
      m_vars.AddVariables( declaration.Execute( null ) ) ;
    } // while

    if ( G.s_scope == 1 ) {
      G.s_allVariableTablesList.push( new ArrayList<VariableTable>() ) ;
      G.s_allVariableTablesList.peek().add( 0, new VariableTable() ) ; // 第0位置不放東西，所以先 add 一個空的變數 table。
    } // if

    // 要趴司 statement 之前，先丟到arrayList內，以便檢查是否宣告
    G.s_allVariableTablesList.peek().add( G.s_scope, new VariableTable( m_vars ) ) ;
    
    if ( Statement.HeadOf( G.s_Context.PeekToken() ) )
      m_statements = new Vector<Statement>() ;
    while ( Statement.HeadOf( G.s_Context.PeekToken() ) ) {
      Statement statement = new Statement() ;
      statement.Parse() ;
      m_statements.add( statement ) ;
    } // while
    // 趴司完 statment 要把這層 scope 的變數刪掉，
    // 但是若這層 scope 是variableTable 最下面，就直接從 stack pop 掉
    if ( G.s_scope == 1 )
      G.s_allVariableTablesList.pop() ;
    else {
      ArrayList<VariableTable> variableTables = G.s_allVariableTablesList.peek() ;
      variableTables.remove( variableTables.size() - 1 ) ;
    } // else

    if ( G.s_Context.PeekToken().equals( "}" ) )
      G.s_Context.GetToken() ;
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public void Execute() throws Exception, ReturnException {
    if ( m_statements != null ) {
      if ( G.s_scope == 1 ) {
        G.s_allVariableTablesList.push( new ArrayList<VariableTable>() ) ;
        G.s_allVariableTablesList.peek().add( 0, new VariableTable() ) ; // 第0位置不放東西，所以先 add 一個空的變數 table。
      } // if

      // 要execute statement 之前，先丟到arrayList內，以便檢查是否宣告
      G.s_allVariableTablesList.peek().add( G.s_scope, new VariableTable( m_vars ) ) ;
      
      for ( int i = 0 ; i < m_statements.size() ; i++ )
        m_statements.get( i ).Execute() ;
      
      // 趴司完 statment 要把這層 scope 的變數刪掉，
      // 但是若這層 scope 是variableTable 最下面，就直接從 stack pop 掉
      if ( G.s_scope == 1 )
        G.s_allVariableTablesList.pop() ;
      else {
        ArrayList<VariableTable> variableTables = G.s_allVariableTablesList.peek() ;
        variableTables.remove( variableTables.size() - 1 ) ;
      } // else
      
    } // if
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return token.equals( "{" ) ;
  } // HeadOf()
} // class Compound_statement implements Node

class Declaration implements Node {
  Type_specifier m_type_specifier = null ;
  Identifier m_id = null ;
  Rest_of_declarators m_rest_of_declarators = null ;
 
  public void Parse() throws Exception {
    String t = null ;
    String ident = null ;
    if ( Type_specifier.HeadOf( G.s_Context.PeekToken() ) ) {
      m_type_specifier = new Type_specifier() ;
      m_type_specifier.Parse() ;
      t = m_type_specifier.Execute() ;
      if ( Identifier.HeadOf( G.s_Context.PeekToken() ) ) {
        m_id = new Identifier() ;
        m_id.Parse() ;
        ident = m_id.Execute() ;
        if ( Rest_of_declarators.HeadOf( G.s_Context.PeekToken() ) ) {
          m_rest_of_declarators = new Rest_of_declarators() ;
          m_rest_of_declarators.Parse( t, ident ) ;
        } // if
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
  
  public HashMap<String, Variable> Execute( String s ) throws Exception {
    return m_rest_of_declarators.Execute() ;
  } // Execute()
  
 
  public static boolean HeadOf( String token ) {
    return Type_specifier.HeadOf( token ) ;
  } // HeadOf()
} // class Declaration implements Node
 
class Statement implements Node {
  final int NULL_STATEMENT = 0 ;
  final int EXPRESSION = 1 ;
  final int RETURN = 2 ;
  final int COMPOUND_STATEMENT = 3 ;
  final int IF_STATEMENT = 4 ;
  final int WHILE_STATEMENT = 5 ;
  final int DO_WHILE_STATEMENT = 6 ;
  
  int m_statementCase = -1 ;
  Expression m_expression = null ;
  Compound_statement m_compoundStatement = null ;
  Statement m_statement = null, m_statement2 = null ;
  
  public void Parse() throws Exception {
    if ( G.s_Context.PeekToken().equals( ";" ) ) {
      G.s_Context.GetToken() ; // skip";"
      m_statementCase = NULL_STATEMENT ;
    } // if
    else if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
      m_statementCase = EXPRESSION ;
      if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
        m_expression = new Expression() ;
        m_expression.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      
      if ( G.s_Context.PeekToken().equals( ";" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else if ( G.s_Context.PeekToken().equals( "return" ) ) {
      m_statementCase = RETURN ;
      G.s_Context.GetToken() ; // skip"return"
      if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
        m_expression = new Expression() ;
        m_expression.Parse() ;
      } // if

      if ( G.s_Context.PeekToken().equals( ";" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else if ( Compound_statement.HeadOf( G.s_Context.PeekToken() ) ) {
      m_statementCase = COMPOUND_STATEMENT ;
      m_compoundStatement = new Compound_statement() ;
      G.s_scope++ ;
      m_compoundStatement.Parse() ;
      G.s_scope-- ;
    } // else if
    else if ( G.s_Context.PeekToken().equals( "if" ) ) {
      m_statementCase = IF_STATEMENT ;
      G.s_Context.GetToken() ; // skip "if"
      if ( G.s_Context.PeekToken().equals( "(" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      
      if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
        m_expression = new Expression() ;
        m_expression.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      
      if ( G.s_Context.PeekToken().equals( ")" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      if ( Statement.HeadOf( G.s_Context.PeekToken() ) ) {
        m_statement = new Statement() ;
        m_statement.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      Error.s_curTokenLine = G.s_line ;
      if ( G.s_Context.PeekToken().equals( "else" ) ) {
        G.s_Context.GetToken() ; // skip "else"
        if ( Statement.HeadOf( G.s_Context.PeekToken() ) ) {
          m_statement2 = new Statement() ;
          m_statement2.Parse() ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // if
      else { // else 若沒有出現，則可能出現行數錯誤的問題發生
        Error.s_theLineErrorInWhichNoElseThere  = true ;
        Error.s_nextTokenLine = G.s_line ;
      } // else
    } // else if
    else if ( G.s_Context.PeekToken().equals( "while" ) ) {
      m_statementCase = WHILE_STATEMENT ;
      G.s_Context.GetToken() ; // skip "while"
      if ( G.s_Context.PeekToken().equals( "(" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      
      if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
        m_expression = new Expression() ;
        m_expression.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      
      if ( G.s_Context.PeekToken().equals( ")" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      if ( Statement.HeadOf( G.s_Context.PeekToken() ) ) {
        m_statement = new Statement() ;
        m_statement.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else if ( G.s_Context.PeekToken().equals( "do" ) ) {
      m_statementCase = DO_WHILE_STATEMENT ;
      G.s_Context.GetToken() ; // skip "do"
      if ( Statement.HeadOf( G.s_Context.PeekToken() ) ) {
        m_statement = new Statement() ;
        m_statement.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      if ( G.s_Context.PeekToken().equals( "while" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle(  G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      if ( G.s_Context.PeekToken().equals( "(" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      
      if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
        m_expression = new Expression() ;
        m_expression.Parse() ;
      } // if
      else

        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      
      if ( G.s_Context.PeekToken().equals( ")" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      if ( G.s_Context.PeekToken().equals( ";" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public void Execute() throws Exception, ReturnException {
    if ( m_statementCase == NULL_STATEMENT )
      return ;
    else if ( m_statementCase == EXPRESSION )
      m_expression.Execute() ;
    else if ( m_statementCase == RETURN ) {
      if ( m_expression != null )
        ReturnException.Handle( ( ( Constant ) m_expression.Execute() ).GetValue() ) ;
      else
        ReturnException.Handle( null ) ;
    } // else if
    else if ( m_statementCase == COMPOUND_STATEMENT ) {
      G.s_scope++ ;
      m_compoundStatement.Execute() ;
      G.s_scope-- ;
    } // else if
    else if ( m_statementCase == IF_STATEMENT ) {
      if ( Boolean.valueOf( ( ( Constant ) m_expression.Execute() ).GetValue() ) )
        m_statement.Execute() ;
      else if ( m_statement2 != null )
        m_statement2.Execute() ;
    } // else if
    else if ( m_statementCase == WHILE_STATEMENT ) {
      while ( Boolean.valueOf( ( ( Constant ) m_expression.Execute() ).GetValue() ) )
        m_statement.Execute() ;
    } // else if
    else if ( m_statementCase == DO_WHILE_STATEMENT ) {
      do {
        m_statement.Execute() ;
      } while ( Boolean.valueOf( ( ( Constant ) m_expression.Execute() ).GetValue() ) ) ;
    } // else if
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return token.equals( ";" ) || Expression.HeadOf( token ) || token.equals( "return" ) ||
           Compound_statement.HeadOf( token ) || token.equals( "if" ) || token.equals( "while" ) ||
           token.equals( "do" ) ;
  } // HeadOf()
} // class Statement implements Node
 
class Expression implements Node {
  Basic_expression m_basicExpression = null ;
  Rest_of_expression m_restOfExpression = null ;
  public void Parse() throws Exception {
    if ( Basic_expression.HeadOf( G.s_Context.PeekToken() ) ) {
      m_basicExpression = new Basic_expression() ;
      m_basicExpression.Parse() ;
      if ( Rest_of_expression.HeadOf( G.s_Context.PeekToken() ) ) {
        m_restOfExpression = new Rest_of_expression() ;
        m_restOfExpression.Parse() ;
      } // if
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public Node Execute() throws Exception {
    if ( m_restOfExpression == null )
      return m_basicExpression.Execute() ;
    else
      return m_restOfExpression.Execute( m_basicExpression.Execute() ) ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Basic_expression.HeadOf( token ) ;
  } // HeadOf()
} // class Expression implements Node

class Rest_of_expression implements Node {
  Expression m_expression = null ;
  Basic_expression m_basicExpression = null ;
  Rest_of_expression m_restOfExpression = null ;
  public void Parse() throws Exception {
    if ( G.s_Context.PeekToken().equals( "," ) ) {
      G.s_Context.GetToken() ; // skip ","
      if ( Basic_expression.HeadOf( G.s_Context.PeekToken() ) ) {
        m_basicExpression = new Basic_expression() ;
        m_basicExpression.Parse() ;
        if ( Rest_of_expression.HeadOf( G.s_Context.PeekToken() ) ) {
          m_restOfExpression = new Rest_of_expression() ;
          m_restOfExpression.Parse() ;
        } // if
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // if
    else if ( G.s_Context.PeekToken().equals( "?" ) ) {
      G.s_Context.GetToken() ; // skip "?"
      if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
        m_expression = new Expression() ;
        m_expression.Parse() ;
        if ( G.s_Context.PeekToken().equals( ":" ) )
          G.s_Context.GetToken() ;
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
        if ( Basic_expression.HeadOf( G.s_Context.PeekToken() ) ) {
          m_basicExpression = new Basic_expression() ;
          m_basicExpression.Parse() ;
          if ( Rest_of_expression.HeadOf( G.s_Context.PeekToken() ) ) {
            m_restOfExpression = new Rest_of_expression() ;
            m_restOfExpression.Parse() ;
          } // if
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public Node Execute( Node node ) throws Exception {
    if ( m_expression == null ) {
      if ( m_restOfExpression == null )
        m_basicExpression.Execute() ;
      else
        m_restOfExpression.Execute( m_basicExpression.Execute() ) ;
      return null ;
    } // if
    else {
      if ( Boolean.valueOf( ( ( Constant ) node ).GetValue() ) )
        return m_expression.Execute() ;
      else {
        if ( m_restOfExpression == null )
          return m_basicExpression.Execute() ;
        else
          return m_restOfExpression.Execute( m_basicExpression.Execute() ) ;
      } // else
    } // else
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return token.equals( "," ) || token.equals( "?" ) ;
  } // HeadOf()
} // class Rest_of_expression implements Node
 
class Basic_expression implements Node {
  Identifier m_id = null ;
  Rest_of_Identifier_started_basic_exp m_restOfISBE = null ;
  Vector<String> m_ppOrmm = null ;
  Vector<String> m_sign = null ;
  Signed_basic_expression m_signedBasicExpression = null ;
  Constant m_constant = null ;
  Expression m_expression = null ;
  Rest_of_maybe_logical_OR_exp m_restOfMaybeLogicalOrExp = null ;
 
  public void Parse() throws Exception {
    if ( Identifier.HeadOf( G.s_Context.PeekToken() ) ) {
      m_id = new Identifier() ;
      m_id.Parse() ;
      if ( !Identifier.HasDefined( m_id ) )
        Error.Handle( m_id.Execute(), Error.UNEXPECTED_TOKEN ) ;
      if ( Rest_of_Identifier_started_basic_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        m_restOfISBE = new Rest_of_Identifier_started_basic_exp() ;
        m_restOfISBE.Parse( m_id ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // if
    else if ( G.s_Context.PeekToken().equals( "++" ) || G.s_Context.PeekToken().equals( "--" ) ) {
      m_ppOrmm = new Vector<String>() ;
      while ( G.s_Context.PeekToken().equals( "++" ) || G.s_Context.PeekToken().equals( "--" ) )
        m_ppOrmm.add( G.s_Context.GetToken() ) ;
      if ( Identifier.HeadOf( G.s_Context.PeekToken() ) ) {
        m_id = new Identifier() ;
        m_id.Parse() ;
        if ( !Identifier.HasDefined( m_id ) )
          Error.Handle( m_id.Execute(), Error.UNEXPECTED_TOKEN ) ;
        if ( Rest_of_Identifier_started_basic_exp.HeadOf( G.s_Context.PeekToken() ) ) {
          m_restOfISBE = new Rest_of_Identifier_started_basic_exp() ;
          m_restOfISBE.Parse( m_id ) ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else if ( Sign.HeadOf( G.s_Context.PeekToken() ) ) {
      m_sign = new Vector<String>() ;
      while ( Sign.HeadOf( G.s_Context.PeekToken() ) )
        m_sign.add( G.s_Context.GetToken() ) ;
      if ( Signed_basic_expression.HeadOf( G.s_Context.PeekToken() ) ) {
        m_signedBasicExpression = new Signed_basic_expression() ;
        m_signedBasicExpression.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else if ( Constant.HeadOf( G.s_Context.PeekToken() ) || G.s_Context.PeekToken().equals( "(" ) ) {
      if ( Constant.HeadOf( G.s_Context.PeekToken() ) ) {
        m_constant = new Constant() ;
        m_constant.Parse() ;
      } // if
      else if ( G.s_Context.PeekToken().equals( "(" ) ) {
        G.s_Context.GetToken() ; // skip "("
        if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
          m_expression = new Expression() ;
          m_expression.Parse() ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
        if ( G.s_Context.PeekToken().equals( ")" ) )
          G.s_Context.GetToken() ;
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // else if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      if ( Rest_of_maybe_logical_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        m_restOfMaybeLogicalOrExp = new Rest_of_maybe_logical_OR_exp() ;
        m_restOfMaybeLogicalOrExp.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public Node Execute() throws Exception {
    if ( m_ppOrmm == null && m_restOfISBE != null )
      return m_restOfISBE.Execute( null, m_id ) ;
    else if ( m_ppOrmm != null && m_restOfISBE != null )
      return m_restOfISBE.Execute( m_ppOrmm, m_id ) ;
    else if ( m_signedBasicExpression != null ) {
      Constant constant = ( Constant ) m_signedBasicExpression.Execute( m_sign ) ;
      return constant ;
    } // else if
    else { // if ( m_restOfMaybeLogicalOrExp != null )
      if ( m_constant != null )
        return m_restOfMaybeLogicalOrExp.Execute( m_constant ) ;
      else
        return m_restOfMaybeLogicalOrExp.Execute( m_expression.Execute() ) ;
    } // else
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Identifier.HeadOf( token ) || token.equals( "++" ) || token.equals( "--" ) ||
           Sign.HeadOf( token ) || Constant.HeadOf( token ) || token.equals( "(" ) ;
  } // HeadOf()
} // class Basic_expression implements Node

// Rest_of_Identifier_started_basic_exp
// 裡面可以是全空，也可會發生混淆不清的狀況
class Rest_of_Identifier_started_basic_exp implements Node {
  Expression m_expression = null ;
  String m_ppOrmm = null ;
  String m_assignmentOperator = null ;
  Expression m_expression2 = null ;
  Rest_of_maybe_logical_OR_exp m_restOfMaybeLogicalOrExp = null ;
  Actual_parameter_list m_actualParameterList = null ;
  boolean m_functionCall = false ;
 
  public void Parse( Identifier id ) throws Exception {
    if ( G.s_Context.PeekToken().equals( "[" ) || G.s_Context.PeekToken().equals( "++" ) ||
         G.s_Context.PeekToken().equals( "--" ) ||
         Assignment_operator.HeadOf( G.s_Context.PeekToken() ) ||
         ( Rest_of_maybe_logical_OR_exp.HeadOf( G.s_Context.PeekToken() ) &&
           !G.s_Context.PeekToken().equals( "(" )
         )
       ) {
      if ( G.s_Context.PeekToken().equals( "[" ) ) {
        G.s_Context.GetToken() ; // skip "["
        if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
          m_expression = new Expression() ;
          m_expression.Parse() ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
        if ( G.s_Context.PeekToken().equals( "]" ) )
          G.s_Context.GetToken() ; // skip "]"
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // if

      if ( G.s_Context.PeekToken().equals( "++" ) || G.s_Context.PeekToken().equals( "--" ) ) {
        m_ppOrmm = G.s_Context.GetToken() ;
      } // if

      if ( Assignment_operator.HeadOf( G.s_Context.PeekToken() ) ) {
        m_assignmentOperator = G.s_Context.GetToken() ;
        if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
          m_expression2 = new Expression() ;
          m_expression2.Parse() ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // if
      else if ( Rest_of_maybe_logical_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        m_restOfMaybeLogicalOrExp = new Rest_of_maybe_logical_OR_exp() ;
        m_restOfMaybeLogicalOrExp.Parse() ;
      } // else if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // if
    else if ( G.s_Context.PeekToken().equals( "(" ) ) {
      m_functionCall = true ;
      if ( FunctionTable.IsSystemSupportFunction( id.Execute() ) )
        FunctionTable.RunSystemSupportFunction( id ) ;
      else {
        G.s_Context.GetToken() ; // skip "("
        if ( Actual_parameter_list.HeadOf( G.s_Context.PeekToken() ) ) {
          m_actualParameterList = new Actual_parameter_list() ;
          m_actualParameterList.Parse() ;
        } // if

        if ( G.s_Context.PeekToken().equals( ")" ) )
          G.s_Context.GetToken() ; // skip ")"
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // else

      if ( Rest_of_maybe_logical_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        m_restOfMaybeLogicalOrExp = new Rest_of_maybe_logical_OR_exp() ;
        m_restOfMaybeLogicalOrExp.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public Node Execute( Vector<String> ppOrmm, Identifier id ) throws Exception {
    String name = id.Execute() ;
    if ( !m_functionCall ) {
      Variable var = null ;
      if ( name.equals( "cin" ) || name.equals( "cout" ) )
        var  = new Variable( name, "0", "cinOrcout", null ) ;
      else
        var = VariableTable.FetchVariable( name ) ;
      if ( m_expression != null ) {
        Constant constant = ( Constant ) m_expression.Execute() ;
        var = var.GetAnArrayElement( Integer.valueOf( constant.GetValue() ) ) ;
      } // if

      if ( ppOrmm != null ) {
        for ( int i = ppOrmm.size() - 1 ; i >= 0 ; i-- ) {
          String operator = ppOrmm.get( i ) ;
          if ( operator.equals( "++" ) )
            var.SetValue( Calculator.Calculate( var, "+", new Constant( "1" ) ).GetValue() ) ;
          else // operator.equals( "--" )
            var.SetValue( Calculator.Calculate( var, "-", new Constant( "1" ) ).GetValue() ) ;
        } // for
      } // if

      if ( m_assignmentOperator != null ) {
        Constant constant = ( Constant ) m_expression2.Execute() ;
        if ( m_assignmentOperator.equals( "+=" ) )
          var.SetValue( Calculator.Calculate( var, "+", constant ).GetValue() ) ;
        else if ( m_assignmentOperator.equals( "-=" ) )
          var.SetValue( Calculator.Calculate( var, "-", constant ).GetValue() ) ;
        else if ( m_assignmentOperator.equals( "*=" ) )
          var.SetValue( Calculator.Calculate( var, "*", constant ).GetValue() ) ;
        else if ( m_assignmentOperator.equals( "/=" ) )
          var.SetValue( Calculator.Calculate( var, "/", constant ).GetValue() ) ;
        else if ( m_assignmentOperator.equals( "%=" ) )
          var.SetValue( Calculator.Calculate( var, "%", constant ).GetValue() ) ;
        else // ( m_assignmentOperator.equals( "=" ) )
          var.SetValue( constant.GetValue() ) ;
        return var ;
      } // if
      else { // m_restOfMaybeLogicalOrExp != null
        Variable copyVariable = var.Copy() ;
        if ( m_ppOrmm != null ) { // 這邊要做 a++的運算
          if ( m_ppOrmm.equals( "++" ) )
            var.SetValue( Calculator.Calculate( var, "+", new Constant( "1" ) ).GetValue() ) ;
          else // operator.equals( "--" )
            var.SetValue( Calculator.Calculate( var, "-", new Constant( "1" ) ).GetValue() ) ;
        } // if

        if ( !m_restOfMaybeLogicalOrExp.IsEmpty() )
          return m_restOfMaybeLogicalOrExp.Execute( copyVariable ) ;
        else // empty.
          return var ;
      } // else
    } // if
    else { // this is pseudo code
      if ( !FunctionTable.IsSystemSupportFunction( name ) ) {
        Function function = FunctionTable.s_allFunction.get( name ) ;
        ArrayList<Node> actualParameterList = m_actualParameterList.Execute() ;
        return function.Call( actualParameterList ) ; // 如果此方選是void,則回傳 null
      } // if
      else
        return null ;
    } // else
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return token.equals( "[" ) || token.equals( "++" ) || token.equals( "--" ) ||
           Assignment_operator.HeadOf( token ) || Rest_of_maybe_logical_OR_exp.HeadOf( token ) ||
           token.equals( "(" ) ;
  } // HeadOf()
} // class Rest_of_Identifier_started_basic_exp implements Node
  
class Signed_basic_expression implements Node {
  Identifier m_id = null ;
  Rest_of_Identifier_started_signed_basic_exp m_restOfIdSSBE = null ;
  Constant m_constant = null ;
  Expression m_expression = null ;
  Rest_of_maybe_logical_OR_exp m_restOfMaybeLogicalOrExp = null ;
  public void Parse() throws Exception {
    if ( Identifier.HeadOf( G.s_Context.PeekToken() ) ) {
      m_id = new Identifier() ;
      m_id.Parse() ;
      if ( !Identifier.HasDefined( m_id ) )
        Error.Handle( m_id.Execute(), Error.UNEXPECTED_TOKEN ) ;

      if ( Rest_of_Identifier_started_basic_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        m_restOfIdSSBE = new Rest_of_Identifier_started_signed_basic_exp() ;
        m_restOfIdSSBE.Parse( m_id ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // if
    else if ( Constant.HeadOf( G.s_Context.PeekToken() ) || G.s_Context.PeekToken().equals( "(" ) ) {
      if ( Constant.HeadOf( G.s_Context.PeekToken() ) ) {
        m_constant = new Constant() ;
        m_constant.Parse() ;
      } // if
      else if ( G.s_Context.PeekToken().equals( "(" ) ) {
        G.s_Context.GetToken() ; // skip "("
        if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
          m_expression = new Expression() ;
          m_expression.Parse() ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
        
        if ( G.s_Context.PeekToken().equals( ")" ) )
          G.s_Context.GetToken() ;
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // else if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      
      if ( Rest_of_maybe_logical_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        m_restOfMaybeLogicalOrExp = new Rest_of_maybe_logical_OR_exp() ;
        m_restOfMaybeLogicalOrExp.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public Node Execute( Vector<String> sign ) throws Exception {
    if ( m_restOfIdSSBE != null )
      return m_restOfIdSSBE.Execute( m_id, sign ) ;
    else if ( m_restOfMaybeLogicalOrExp != null ) {
      Constant constant = ( Constant ) ( m_constant != null ? m_constant : m_expression.Execute() ) ;
      for ( int i = 0 ; i < sign.size() ; i++ ) {
        if ( sign.get( i ).equals( "!" ) )
          constant = Calculator.Calculate( constant, "!", null ) ;
        else if ( sign.get( i ).equals( "+" ) )
          constant = Calculator.Calculate( constant, "+", null ) ;
        else // if ( sign.get( i ).equals( "-" ) )
          constant = Calculator.Calculate( constant, "-", null ) ;
      } // for

      return m_restOfMaybeLogicalOrExp.Execute( constant ) ;
    } // else if

    return null ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Identifier.HeadOf( token ) || Constant.HeadOf( token ) || token.equals( "(" ) ;
  } // HeadOf()
} // class Signed_basic_expression implements Node
 
class Rest_of_Identifier_started_signed_basic_exp implements Node {
  Expression m_expression = null ;
  String m_ppOrmm = null ;
  Rest_of_maybe_logical_OR_exp m_restOfMaybeLogicalOrExp = null ;
  Actual_parameter_list m_actualParameterList = null ;
  boolean m_functionCall = false ;
  public void Parse( Identifier id ) throws Exception {
    if ( G.s_Context.PeekToken().equals( "[" ) || G.s_Context.PeekToken().equals( "++" ) ||
         G.s_Context.PeekToken().equals( "--" ) ||
         ( Rest_of_maybe_logical_OR_exp.HeadOf( G.s_Context.PeekToken() ) &&
           !G.s_Context.PeekToken().equals( "(" )
         )
       ) {
      if ( G.s_Context.PeekToken().equals( "[" ) ) {
        G.s_Context.GetToken() ; // skip "["
        m_expression = new Expression() ;
        m_expression.Parse() ;
        if ( G.s_Context.PeekToken().equals( "]" ) )
          G.s_Context.GetToken() ; // skip "]"
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // if
     
      if ( G.s_Context.PeekToken().equals( "++" ) || G.s_Context.PeekToken().equals( "--" ) ) {
        m_ppOrmm = G.s_Context.GetToken() ;
      } // if
      
      if ( Rest_of_maybe_logical_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        m_restOfMaybeLogicalOrExp = new Rest_of_maybe_logical_OR_exp() ;
        m_restOfMaybeLogicalOrExp.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // if
    else if ( G.s_Context.PeekToken().equals( "(" ) ) {
      m_functionCall = true ;
      G.s_Context.GetToken() ; // skip "("
      if ( Actual_parameter_list.HeadOf( G.s_Context.PeekToken() ) ) {
        m_actualParameterList = new Actual_parameter_list() ;
        m_actualParameterList.Parse() ;
      } // if
      
      if ( G.s_Context.PeekToken().equals( ")" ) )
        G.s_Context.GetToken() ; // skip ")"
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      
      if ( Rest_of_maybe_logical_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        m_restOfMaybeLogicalOrExp = new Rest_of_maybe_logical_OR_exp() ;
        m_restOfMaybeLogicalOrExp.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public Node Execute( Identifier id, Vector<String> sign  ) throws Exception {
    String name = id.Execute() ;
    if ( !m_functionCall ) {
      Variable var = VariableTable.FetchVariable( name ) ;
      if ( m_expression != null ) {
        Constant constant = ( Constant ) m_expression.Execute() ;
        var = var.GetAnArrayElement( Integer.valueOf( constant.GetValue() ) ) ;
      } // if

      Variable copyVariable = var.Copy() ;
      if ( m_ppOrmm != null ) { // 這邊要做 a++的運算
        if ( m_ppOrmm.equals( "++" ) )
          var.SetValue( Calculator.Calculate( var, "+", new Constant( "1" ) ).GetValue() ) ;
        else // operator.equals( "--" )
          var.SetValue( Calculator.Calculate( var, "-", new Constant( "1" ) ).GetValue() ) ;
      } // if

      for ( int i = 0 ; i < sign.size() ; i++ ) {
        if ( sign.get( i ).equals( "!" ) )
          copyVariable.SetValue( Calculator.Calculate( copyVariable, "!", null ).GetValue() ) ;
        else if ( sign.equals( "+" ) )
          copyVariable.SetValue( Calculator.Calculate( copyVariable, "+", null ).GetValue() ) ;
        else // if ( sign.equals( "-" ) )
          copyVariable.SetValue( Calculator.Calculate( copyVariable, "-", null ).GetValue() ) ;
      } // for

      return m_restOfMaybeLogicalOrExp.Execute( copyVariable ) ;
    } // if
    else { // this is pseudo code
      Function function = FunctionTable.s_allFunction.get( name ) ;
      ArrayList<Node> actualParameterList = m_actualParameterList.Execute() ;
      Constant constant = ( Constant ) function.Call( actualParameterList ) ;
      Constant constant2 = null ;
      if ( constant != null ) {
        for ( int i = 0 ; i < sign.size() ; i++ ) {
          if ( sign.get( i ).equals( "!" ) )
            constant = Calculator.Calculate( constant, "!", null ) ;
          else if ( sign.equals( "+" ) )
            constant = Calculator.Calculate( constant, "+", null ) ;
          else // if ( sign.equals( "-" ) )
            constant = Calculator.Calculate( constant, "-", null ) ;
        } // for

        constant2 = ( Constant ) m_restOfMaybeLogicalOrExp.Execute( constant ) ;
      } // if

      return constant2 != null ? constant2 : constant ; // 如果此方選回傳是void,則回傳 null
    } // else
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return token.equals( "[" ) || token.equals( "++" ) || token.equals( "--" ) ||
           Rest_of_maybe_logical_OR_exp.HeadOf( token ) || token.equals( "(" ) ;
  } // HeadOf()
} // class Rest_of_Identifier_started_signed_basic_exp implements Node
 
class Sign implements Node {
 
  public static boolean HeadOf( String token ) {
    return token.equals( "+" ) || token.equals( "-" ) || token.equals( "!" ) ;
  } // HeadOf()
} // class Sign implements Node
 
class Actual_parameter_list implements Node {
  ArrayList<Non_comma_expression> m_nonCommaExpressions = null ;
  
  public void Parse() throws Exception {
    m_nonCommaExpressions = new ArrayList<Non_comma_expression>() ;
    Non_comma_expression nonCommaExpression = null ;
    if ( Non_comma_expression.HeadOf( G.s_Context.PeekToken() ) ) {
      nonCommaExpression = new Non_comma_expression() ;
      nonCommaExpression.Parse() ;
      m_nonCommaExpressions.add( nonCommaExpression ) ;
      while ( G.s_Context.PeekToken().equals( "," ) ) {
        G.s_Context.GetToken() ; // skip ","
        if ( Non_comma_expression.HeadOf( G.s_Context.PeekToken() ) ) {
          nonCommaExpression = new Non_comma_expression() ;
          nonCommaExpression.Parse() ;
          m_nonCommaExpressions.add( nonCommaExpression ) ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // while
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public ArrayList<Node> Execute() throws Exception {
    ArrayList<Node> nodes = new ArrayList<Node>() ;
    for ( int i = 0 ; i < m_nonCommaExpressions.size() ; i++ )
      nodes.add( m_nonCommaExpressions.get( i ).Execute() ) ;
    return nodes ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Non_comma_expression.HeadOf( token ) ;
  } // HeadOf()
} // class Actual_parameter_list implements Node
 
class Non_comma_expression implements Node {
  Basic_expression m_basicExpression = null ;
  Rest_of_non_comma_expression m_restOfNonCommaExpression = null ;
  public void Parse() throws Exception {
    if ( Basic_expression.HeadOf( G.s_Context.PeekToken() ) ) {
      m_basicExpression = new Basic_expression() ;
      m_basicExpression.Parse() ;
      if ( Rest_of_non_comma_expression.HeadOf( G.s_Context.PeekToken() ) ) {
        m_restOfNonCommaExpression = new Rest_of_non_comma_expression() ;
        m_restOfNonCommaExpression.Parse() ;
      } // if
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public Node Execute() throws Exception {
    if ( m_restOfNonCommaExpression == null )
      return m_basicExpression.Execute() ;
    else
      return m_restOfNonCommaExpression.Execute( m_basicExpression.Execute() ) ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Basic_expression.HeadOf( token ) ;
  } // HeadOf()
} // class Non_comma_expression implements Node
 
class Rest_of_non_comma_expression implements Node {
  Expression m_expression = null ;
  Basic_expression m_basicExpression = null ;
  Rest_of_non_comma_expression m_restOfNonCommaExpression = null ;
  public void Parse() throws Exception {
    if ( G.s_Context.PeekToken().equals( "?" ) )
      G.s_Context.GetToken() ; // skip "?"
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    
    if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
      m_expression = new Expression() ;
      m_expression.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    
    if ( G.s_Context.PeekToken().equals( ":" ) )
      G.s_Context.GetToken() ; // skip ":"
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    
    if ( Basic_expression.HeadOf( G.s_Context.PeekToken() ) ) {
      m_basicExpression = new Basic_expression() ;
      m_basicExpression.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    
    if ( Rest_of_non_comma_expression.HeadOf( G.s_Context.PeekToken() ) ) {
      m_restOfNonCommaExpression = new Rest_of_non_comma_expression() ;
      m_restOfNonCommaExpression.Parse() ;
    } // if
  } // Parse()
 
  public Node Execute( Node node ) throws Exception {
    if ( Boolean.valueOf( ( ( Constant ) node ).GetValue() ) )
      return m_basicExpression.Execute() ;
    else {
      if ( m_restOfNonCommaExpression == null )
        return m_basicExpression.Execute() ;
      else
        return m_restOfNonCommaExpression.Execute( m_basicExpression.Execute() ) ;
    } // else
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return token.equals( "?" ) ;
  } // HeadOf()
} // class Rest_of_non_comma_expression implements Node
 
class Assignment_operator implements Node {
 
  public static boolean HeadOf( String token ) {
    return token.equals( "=" ) || token.equals( "*=" ) || token.equals( "/=" ) ||
           token.equals( "%=" ) || token.equals( "+=" ) || token.equals( "-=" ) ;
  } // HeadOf()
} // class Assignment_operator implements Node
 
class Rest_of_maybe_logical_OR_exp implements Node {
  Rest_of_maybe_logical_AND_exp m_restOfMaybeLogicalAndExp = null ;
  ArrayList<Maybe_logical_AND_exp> m_maybeLogicalAndExps = null ;
  boolean m_isEmpty = false ;
  public void Parse() throws Exception {
    if ( G.s_Context.PeekToken().equals( "," ) || G.s_Context.PeekToken().equals( "?" ) ||
         G.s_Context.PeekToken().equals( ";" ) ) {
      m_isEmpty = true ;
    } // if

    if ( Rest_of_maybe_logical_AND_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      m_restOfMaybeLogicalAndExp = new Rest_of_maybe_logical_AND_exp() ;
      m_restOfMaybeLogicalAndExp.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "||" ) ) {
      G.s_Context.GetToken() ; // skip "||"
      if ( m_maybeLogicalAndExps == null )
        m_maybeLogicalAndExps = new ArrayList<Maybe_logical_AND_exp>() ;
      
      if ( Maybe_logical_AND_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        Maybe_logical_AND_exp maybeLogicalAndExp = new Maybe_logical_AND_exp() ;
        maybeLogicalAndExp.Parse() ;
        m_maybeLogicalAndExps.add( maybeLogicalAndExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute( Node node ) throws Exception {
    Constant constant1 = ( Constant ) m_restOfMaybeLogicalAndExp.Execute( node ) ;
    if ( m_maybeLogicalAndExps != null ) {
      for ( int i = 0 ; i < m_maybeLogicalAndExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeLogicalAndExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, "||", constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Rest_of_maybe_logical_AND_exp.HeadOf( token ) ;
  } // HeadOf()
  
  public boolean IsEmpty() {
    return m_isEmpty ;
  } // IsEmpty()
} // class Rest_of_maybe_logical_OR_exp implements Node
 
class Maybe_logical_AND_exp implements Node {
  Vector<Maybe_bit_OR_exp> m_maybeBitOrExps = null ;

  public void Parse() throws Exception {
    Maybe_bit_OR_exp maybeBitOrExp = null ;
    m_maybeBitOrExps = new Vector<Maybe_bit_OR_exp>() ;
    if ( Maybe_bit_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      maybeBitOrExp = new Maybe_bit_OR_exp() ;
      maybeBitOrExp.Parse() ;
      m_maybeBitOrExps.add( maybeBitOrExp ) ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "&&" ) ) {
      G.s_Context.GetToken() ; // skip "&&"
      if ( Maybe_bit_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        maybeBitOrExp = new Maybe_bit_OR_exp() ;
        maybeBitOrExp.Parse() ;
        m_maybeBitOrExps.add( maybeBitOrExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute() throws Exception {
    Constant constant1 = ( Constant ) m_maybeBitOrExps.get( 0 ).Execute() ;
    if ( m_maybeBitOrExps.size() > 1 ) {
      for ( int i = 1 ; i < m_maybeBitOrExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeBitOrExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, "&&", constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Maybe_bit_OR_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Maybe_logical_AND_exp implements Node
 
class Rest_of_maybe_logical_AND_exp implements Node {
  Rest_of_maybe_bit_OR_exp m_restOfMaybeBitOrExp = null ;
  Vector<Maybe_bit_OR_exp> m_maybeBitOrExps = null ;

  public void Parse() throws Exception {
    if ( Rest_of_maybe_bit_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      m_restOfMaybeBitOrExp = new Rest_of_maybe_bit_OR_exp() ;
      m_restOfMaybeBitOrExp.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "&&" ) ) {
      G.s_Context.GetToken() ; // skip "&&"
      if ( m_maybeBitOrExps == null )
        m_maybeBitOrExps = new Vector<Maybe_bit_OR_exp>() ;
      
      if ( Maybe_bit_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        Maybe_bit_OR_exp maybeBitOrExp = new Maybe_bit_OR_exp() ;
        maybeBitOrExp.Parse() ;
        m_maybeBitOrExps.add( maybeBitOrExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute( Node node ) throws Exception {
    Constant constant1 = ( Constant ) m_restOfMaybeBitOrExp.Execute( node ) ;
    if ( m_maybeBitOrExps != null ) {
      for ( int i = 0 ; i < m_maybeBitOrExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeBitOrExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, "&&", constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Rest_of_maybe_bit_OR_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Rest_of_maybe_logical_AND_exp implements Node
 
class Maybe_bit_OR_exp implements Node {
  Vector<Maybe_bit_ex_OR_exp> m_maybeBitExOrExps = null ;

  public void Parse() throws Exception {
    Maybe_bit_ex_OR_exp maybeBitExOrExp = null ;
    m_maybeBitExOrExps = new Vector<Maybe_bit_ex_OR_exp>() ;
    if ( Maybe_bit_ex_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      maybeBitExOrExp = new Maybe_bit_ex_OR_exp() ;
      maybeBitExOrExp.Parse() ;
      m_maybeBitExOrExps.add( maybeBitExOrExp ) ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "|" ) ) {
      G.s_Context.GetToken() ; // skip "|"
      if ( Maybe_bit_ex_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        maybeBitExOrExp = new Maybe_bit_ex_OR_exp() ;
        maybeBitExOrExp.Parse() ;
        m_maybeBitExOrExps.add( maybeBitExOrExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute() throws Exception {
    Constant constant1 = ( Constant ) m_maybeBitExOrExps.get( 0 ).Execute() ;
    if ( m_maybeBitExOrExps.size() > 1 ) {
      for ( int i = 1 ; i < m_maybeBitExOrExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeBitExOrExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, "|", constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Maybe_bit_ex_OR_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Maybe_bit_OR_exp implements Node
 
class Rest_of_maybe_bit_OR_exp implements Node {
  Rest_of_maybe_bit_ex_OR_exp m_restOfMaybeBitExOrExp = null ;
  Vector<Maybe_bit_ex_OR_exp> m_maybeBitExOrExps = null ;

  public void Parse() throws Exception {
    if ( Rest_of_maybe_bit_ex_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      m_restOfMaybeBitExOrExp = new Rest_of_maybe_bit_ex_OR_exp() ;
      m_restOfMaybeBitExOrExp.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "|" ) ) {
      G.s_Context.GetToken() ; // skip "|"
      if ( m_maybeBitExOrExps == null )
        m_maybeBitExOrExps = new Vector<Maybe_bit_ex_OR_exp>() ;
      
      if ( Maybe_bit_ex_OR_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        Maybe_bit_ex_OR_exp maybeBitExOrExp = new Maybe_bit_ex_OR_exp() ;
        maybeBitExOrExp.Parse() ;
        m_maybeBitExOrExps.add( maybeBitExOrExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute( Node node ) throws Exception {
    Constant constant1 = ( Constant ) m_restOfMaybeBitExOrExp.Execute( node ) ;
    if ( m_maybeBitExOrExps != null ) {
      for ( int i = 0 ; i < m_maybeBitExOrExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeBitExOrExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, "|", constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Rest_of_maybe_bit_ex_OR_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Rest_of_maybe_bit_OR_exp implements Node
 
class Maybe_bit_ex_OR_exp implements Node {
  Vector<Maybe_bit_AND_exp> m_maybeBitAndExps = null ;
  
  public void Parse() throws Exception {
    Maybe_bit_AND_exp maybeBitAndExp = null ;
    m_maybeBitAndExps = new Vector<Maybe_bit_AND_exp>() ;
    if ( Maybe_bit_AND_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      maybeBitAndExp = new Maybe_bit_AND_exp() ;
      maybeBitAndExp.Parse() ;
      m_maybeBitAndExps.add( maybeBitAndExp ) ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "^" ) ) {
      G.s_Context.GetToken() ; // skip "^"
      if ( Maybe_bit_AND_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        maybeBitAndExp = new Maybe_bit_AND_exp() ;
        maybeBitAndExp.Parse() ;
        m_maybeBitAndExps.add( maybeBitAndExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute() throws Exception {
    Constant constant1 = ( Constant ) m_maybeBitAndExps.get( 0 ).Execute() ;
    if ( m_maybeBitAndExps.size() > 1 ) {
      for ( int i = 1 ; i < m_maybeBitAndExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeBitAndExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, "^", constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Maybe_bit_AND_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Maybe_bit_ex_OR_exp implements Node
 
class Rest_of_maybe_bit_ex_OR_exp implements Node {
  Rest_of_maybe_bit_AND_exp m_restOfMaybeBitAndExp = null ;
  Vector<Maybe_bit_AND_exp> m_maybeBitAndExps = null ;

  public void Parse() throws Exception {
    if ( Rest_of_maybe_bit_AND_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      m_restOfMaybeBitAndExp = new Rest_of_maybe_bit_AND_exp() ;
      m_restOfMaybeBitAndExp.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "^" ) ) {
      G.s_Context.GetToken() ; // skip "^"
      if ( m_maybeBitAndExps == null )
        m_maybeBitAndExps = new Vector<Maybe_bit_AND_exp>() ;
      
      if ( Maybe_bit_AND_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        Maybe_bit_AND_exp maybeBitAndExp = new Maybe_bit_AND_exp() ;
        maybeBitAndExp.Parse() ;
        m_maybeBitAndExps.add( maybeBitAndExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute( Node node ) throws Exception {
    Constant constant1 = ( Constant ) m_restOfMaybeBitAndExp.Execute( node ) ;
    if ( m_maybeBitAndExps != null ) {
      for ( int i = 0 ; i < m_maybeBitAndExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeBitAndExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, "^", constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Rest_of_maybe_bit_AND_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Rest_of_maybe_bit_ex_OR_exp implements Node
 
class Maybe_bit_AND_exp implements Node {
  Vector<Maybe_equality_exp> m_maybeEqualityExps = null ;

  public void Parse() throws Exception {
    Maybe_equality_exp maybeEqualityExp = null ;
    m_maybeEqualityExps = new Vector<Maybe_equality_exp>() ;
    if ( Maybe_equality_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      maybeEqualityExp = new Maybe_equality_exp() ;
      maybeEqualityExp.Parse() ;
      m_maybeEqualityExps.add( maybeEqualityExp ) ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "&" ) ) {
      G.s_Context.GetToken() ; // skip "&"
      if ( Maybe_equality_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        maybeEqualityExp = new Maybe_equality_exp() ;
        maybeEqualityExp.Parse() ;
        m_maybeEqualityExps.add( maybeEqualityExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute() throws Exception {
    Constant constant1 = ( Constant ) m_maybeEqualityExps.get( 0 ).Execute() ;
    if ( m_maybeEqualityExps.size() > 1 ) {
      for ( int i = 1 ; i < m_maybeEqualityExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeEqualityExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, "&", constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Maybe_equality_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Maybe_bit_AND_exp implements Node
 
class Rest_of_maybe_bit_AND_exp implements Node {
  Rest_of_maybe_equality_exp m_restOfMaybeEqualityExp = null ;
  Vector<Maybe_equality_exp> m_maybeEqualityExps = null ;

  public void Parse() throws Exception {
    if ( Rest_of_maybe_equality_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      m_restOfMaybeEqualityExp = new Rest_of_maybe_equality_exp() ;
      m_restOfMaybeEqualityExp.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "&" ) ) {
      G.s_Context.GetToken() ; // skip "&"
      if ( m_maybeEqualityExps == null )
        m_maybeEqualityExps = new Vector<Maybe_equality_exp>() ;
      
      if ( Maybe_equality_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        Maybe_equality_exp maybeEqualityExp = new Maybe_equality_exp() ;
        maybeEqualityExp.Parse() ;
        m_maybeEqualityExps.add( maybeEqualityExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute( Node node ) throws Exception {
    Constant constant1 = ( Constant ) m_restOfMaybeEqualityExp.Execute( node ) ;
    if ( m_maybeEqualityExps != null ) {
      for ( int i = 0 ; i < m_maybeEqualityExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeEqualityExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, "&", constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Rest_of_maybe_equality_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Rest_of_maybe_bit_AND_exp implements Node
 
class Maybe_equality_exp implements Node {
  Vector<String> m_eqOrNeq = null ;
  Vector<Maybe_relational_exp> m_maybeRelationalExps = null ;

  public void Parse() throws Exception {
    Maybe_relational_exp maybeRelationalExp = null ;
    m_maybeRelationalExps = new Vector<Maybe_relational_exp>() ;
    if ( Maybe_relational_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      maybeRelationalExp = new Maybe_relational_exp() ;
      maybeRelationalExp.Parse() ;
      m_maybeRelationalExps.add( maybeRelationalExp ) ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "==" ) || G.s_Context.PeekToken().equals( "!=" ) ) {
      if ( m_eqOrNeq == null )
        m_eqOrNeq = new Vector<String>() ;
      m_eqOrNeq.add( G.s_Context.GetToken() ) ;
      
      if ( Maybe_relational_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        maybeRelationalExp = new Maybe_relational_exp() ;
        maybeRelationalExp.Parse() ;
        m_maybeRelationalExps.add( maybeRelationalExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute() throws Exception {
    Constant constant1 = ( Constant ) m_maybeRelationalExps.get( 0 ).Execute() ;
    if ( m_maybeRelationalExps.size() > 1 ) {
      for ( int i = 1 ; i < m_maybeRelationalExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeRelationalExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, m_eqOrNeq.get( i - 1 ), constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Maybe_relational_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Maybe_equality_exp implements Node
 
class Rest_of_maybe_equality_exp implements Node {
  Rest_of_maybe_relational_exp m_restOfMaybeRelationalExp = null ;
  Vector<String> m_eqOrNeq = null ;
  Vector<Maybe_relational_exp> m_maybeRelationalExps = null ;

  public void Parse() throws Exception {
    if ( Rest_of_maybe_relational_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      m_restOfMaybeRelationalExp = new Rest_of_maybe_relational_exp() ;
      m_restOfMaybeRelationalExp.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "==" ) || G.s_Context.PeekToken().equals( "!=" ) ) {
      if ( m_eqOrNeq == null )
        m_eqOrNeq = new Vector<String>() ;
      m_eqOrNeq.add( G.s_Context.GetToken() ) ;
      
      if ( m_maybeRelationalExps == null )
        m_maybeRelationalExps = new Vector<Maybe_relational_exp>() ;
      
      if ( Maybe_relational_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        Maybe_relational_exp maybeRelationalExp = new Maybe_relational_exp() ;
        maybeRelationalExp.Parse() ;
        m_maybeRelationalExps.add( maybeRelationalExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute( Node node ) throws Exception {
    Constant constant1 = ( Constant ) m_restOfMaybeRelationalExp.Execute( node ) ;
    if ( m_maybeRelationalExps != null ) {
      for ( int i = 0 ; i < m_maybeRelationalExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeRelationalExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, m_eqOrNeq.get( i ), constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Rest_of_maybe_relational_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Rest_of_maybe_equality_exp implements Node
 
class Maybe_relational_exp implements Node {
  
  Vector<String> m_lessOrMore = null ;
  Vector<Maybe_shift_exp> m_maybeShiftExps = null ;

  public void Parse() throws Exception {
    Maybe_shift_exp maybeShiftExp = null ;
    m_maybeShiftExps = new Vector<Maybe_shift_exp>() ;
    if ( Maybe_shift_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      maybeShiftExp = new Maybe_shift_exp() ;
      // Type_specifier.boolExpressionChecker = true ;
      maybeShiftExp.Parse() ;
      // if ( !Type_specifier.isBoolExpression() )
        // Error.Handle( null, Error.TYPE_ERROR ) ;
      // Type_specifier.boolExpressionChecker = false ;
      // Type_specifier.s_tokenCollections = new Vector<String>() ;
      m_maybeShiftExps.add( maybeShiftExp ) ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    
    while ( G.s_Context.PeekToken().equals( "<" ) || G.s_Context.PeekToken().equals( "<=" ) ||
            G.s_Context.PeekToken().equals( ">" ) || G.s_Context.PeekToken().equals( ">=" ) ) {
      if ( m_lessOrMore == null )
        m_lessOrMore = new Vector<String>() ;
      m_lessOrMore.add( G.s_Context.GetToken() ) ;
      
      if ( Maybe_shift_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        maybeShiftExp = new Maybe_shift_exp() ;
        maybeShiftExp.Parse() ;
        m_maybeShiftExps.add( maybeShiftExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute() throws Exception {
    Constant constant1 = ( Constant ) m_maybeShiftExps.get( 0 ).Execute() ;
    if ( m_maybeShiftExps.size() > 1 ) {
      for ( int i = 1 ; i < m_maybeShiftExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeShiftExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, m_lessOrMore.get( i - 1 ), constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Maybe_shift_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Maybe_relational_exp implements Node
 
class Rest_of_maybe_relational_exp implements Node {
  Rest_of_maybe_shift_exp m_restOfMaybeShiftExp = null ;
  Vector<String> m_lessOrMore = null ;
  Vector<Maybe_shift_exp> m_maybeShiftExps = null ;

  public void Parse() throws Exception {
    if ( Rest_of_maybe_shift_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      m_restOfMaybeShiftExp = new Rest_of_maybe_shift_exp() ;
      m_restOfMaybeShiftExp.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "<" ) || G.s_Context.PeekToken().equals( "<=" ) ||
            G.s_Context.PeekToken().equals( ">" ) || G.s_Context.PeekToken().equals( ">=" ) ) {
      if ( m_lessOrMore == null )
        m_lessOrMore = new Vector<String>() ;
      m_lessOrMore.add( G.s_Context.GetToken() ) ;
      
      if ( m_maybeShiftExps == null )
        m_maybeShiftExps = new Vector<Maybe_shift_exp>() ;
      
      if ( Maybe_shift_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        Maybe_shift_exp maybeShiftExp = new Maybe_shift_exp() ;
        maybeShiftExp.Parse() ;
        m_maybeShiftExps.add( maybeShiftExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute( Node node ) throws Exception {
    Constant constant1 = ( Constant ) m_restOfMaybeShiftExp.Execute( node ) ;
    if ( m_maybeShiftExps != null ) {
      for ( int i = 0 ; i < m_maybeShiftExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeShiftExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, m_lessOrMore.get( i ), constant2 ) ;
      } // for
    } // if
    
    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Rest_of_maybe_shift_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Rest_of_maybe_relational_exp implements Node
 
class Maybe_shift_exp implements Node {
  Vector<String> m_shifts = null ;
  Vector<Maybe_additive_exp> m_maybeAdditiveExps = null ;

  public void Parse() throws Exception {
    Maybe_additive_exp maybeAdditiveExp = null ;
    m_maybeAdditiveExps = new Vector<Maybe_additive_exp>() ;
    if ( Maybe_additive_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      maybeAdditiveExp = new Maybe_additive_exp() ;
      maybeAdditiveExp.Parse() ;
      m_maybeAdditiveExps.add( maybeAdditiveExp ) ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "<<" ) || G.s_Context.PeekToken().equals( ">>" ) ) {
      if ( m_shifts == null )
        m_shifts = new Vector<String>() ;
      m_shifts.add( G.s_Context.GetToken() ) ;
      
      if ( Maybe_additive_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        maybeAdditiveExp = new Maybe_additive_exp() ;
        maybeAdditiveExp.Parse() ;
        m_maybeAdditiveExps.add( maybeAdditiveExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute() throws Exception {
    Constant constant1 = ( Constant ) m_maybeAdditiveExps.get( 0 ).Execute() ;
    if ( m_maybeAdditiveExps.size() > 1 ) {
      for ( int i = 1 ; i < m_maybeAdditiveExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeAdditiveExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, m_shifts.get( i - 1 ), constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Maybe_additive_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Maybe_shift_exp implements Node
 
class Rest_of_maybe_shift_exp implements Node {
  Rest_of_maybe_additive_exp m_restOfMaybeAdditiveExp = null ;
  Vector<String> m_shifts = null ;
  Vector<Maybe_additive_exp> m_maybeAdditiveExps = null ;

  public void Parse() throws Exception {
    if ( Rest_of_maybe_additive_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      m_restOfMaybeAdditiveExp = new Rest_of_maybe_additive_exp() ;
      m_restOfMaybeAdditiveExp.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "<<" ) || G.s_Context.PeekToken().equals( ">>" ) ) {
      if ( m_shifts == null )
        m_shifts = new Vector<String>() ;
      m_shifts.add( G.s_Context.GetToken() ) ;
      if ( m_maybeAdditiveExps == null )
        m_maybeAdditiveExps = new Vector<Maybe_additive_exp>() ;
      
      if ( Maybe_additive_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        Maybe_additive_exp maybeAdditiveExp = new Maybe_additive_exp() ;
        maybeAdditiveExp.Parse() ;
        m_maybeAdditiveExps.add( maybeAdditiveExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute( Node node ) throws Exception {
    if ( node instanceof Variable && ( ( Variable ) node ).m_name.equals( "cout" ) ) {
      
      for ( int i = 0 ; i < m_maybeAdditiveExps.size() ; i++ ) {
        Constant constant = ( Constant ) m_maybeAdditiveExps.get( i ).Execute() ;
        if ( constant.GetType().equals( "float" ) )
          System.out.print( ( int ) Math.floor( Float.valueOf( constant.GetValue() ) ) ) ;
        else
          System.out.printf( constant.GetValue() ) ;
      } // for

      return null ;
    } // if
    else {
      Constant constant1 = ( Constant ) m_restOfMaybeAdditiveExp.Execute( node ) ;
      if ( m_maybeAdditiveExps != null ) {
        for ( int i = 0 ; i < m_maybeAdditiveExps.size() ; i++ ) {
          Constant constant2 = ( Constant ) m_maybeAdditiveExps.get( i ).Execute() ;
          constant1 = Calculator.Calculate( constant1, m_shifts.get( i ), constant2 ) ;
        } // for
      } // if
      
      return constant1 ;
    } // else
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Rest_of_maybe_additive_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Rest_of_maybe_shift_exp implements Node
 
class Maybe_additive_exp implements Node {
  
  Vector<String> m_plusOrLess = null ;
  Vector<Maybe_mult_exp> m_maybeMultExps = null ;

  public void Parse() throws Exception {
    Maybe_mult_exp maybeMultExp = null ;
    m_maybeMultExps = new Vector<Maybe_mult_exp>() ;
    if ( Maybe_mult_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      maybeMultExp = new Maybe_mult_exp() ;
      maybeMultExp.Parse() ;
      m_maybeMultExps.add( maybeMultExp ) ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "+" ) || G.s_Context.PeekToken().equals( "-" ) ) {
      if ( m_plusOrLess == null )
        m_plusOrLess = new Vector<String>() ;
      m_plusOrLess.add( G.s_Context.GetToken() ) ;
      
      if ( Maybe_mult_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        maybeMultExp = new Maybe_mult_exp() ;
        maybeMultExp.Parse() ;
        m_maybeMultExps.add( maybeMultExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute() throws Exception {
    Constant constant1 = ( Constant ) m_maybeMultExps.get( 0 ).Execute() ;
    if ( m_maybeMultExps.size() > 1 ) {
      for ( int i = 1 ; i < m_maybeMultExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeMultExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, m_plusOrLess.get( i - 1 ), constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Maybe_mult_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Maybe_additive_exp implements Node
 
class Rest_of_maybe_additive_exp implements Node {
  Rest_of_maybe_mult_exp m_restOfMaybeMultExp = null ;
  Vector<String> m_plusOrLess = null ;
  Vector<Maybe_mult_exp> m_maybeMultExps = null ;

  public void Parse() throws Exception {
    if ( Rest_of_maybe_mult_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      m_restOfMaybeMultExp = new Rest_of_maybe_mult_exp() ;
      m_restOfMaybeMultExp.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    while ( G.s_Context.PeekToken().equals( "+" ) || G.s_Context.PeekToken().equals( "-" ) ) {
      if ( m_plusOrLess == null )
        m_plusOrLess = new Vector<String>() ;
      m_plusOrLess.add( G.s_Context.GetToken() ) ;
      if ( m_maybeMultExps == null )
        m_maybeMultExps = new Vector<Maybe_mult_exp>() ;
      
      if ( Maybe_mult_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        Maybe_mult_exp maybeMultExp = new Maybe_mult_exp() ;
        maybeMultExp.Parse() ;
        m_maybeMultExps.add( maybeMultExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute( Node node ) throws Exception {
    Constant constant1 = ( Constant ) m_restOfMaybeMultExp.Execute( node ) ;
    if ( m_maybeMultExps != null ) {
      for ( int i = 0 ; i < m_maybeMultExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_maybeMultExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, m_plusOrLess.get( i ), constant2 ) ;
      } // for
    } // if

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Rest_of_maybe_mult_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Rest_of_maybe_additive_exp implements Node
 
class Maybe_mult_exp implements Node {
  Unary_exp m_unaryExp = null ;
  Rest_of_maybe_mult_exp m_restOfMaybeMultExp = null ;
  
  public void Parse() throws Exception {
    if ( Unary_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      m_unaryExp = new Unary_exp() ;
      m_unaryExp.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;

    if ( Rest_of_maybe_mult_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      m_restOfMaybeMultExp = new Rest_of_maybe_mult_exp() ;
      m_restOfMaybeMultExp.Parse() ;
    } // if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public Node Execute() throws Exception {
    return m_restOfMaybeMultExp.Execute( m_unaryExp.Execute() ) ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Unary_exp.HeadOf( token ) ;
  } // HeadOf()
} // class Maybe_mult_exp implements Node
 
class Rest_of_maybe_mult_exp implements Node {
  Vector<String> m_operators = null ;
  Vector<Unary_exp> m_unaryExps = null ;
  public void Parse() throws Exception {
    while ( G.s_Context.PeekToken().equals( "*" ) || G.s_Context.PeekToken().equals( "/" ) ||
            G.s_Context.PeekToken().equals( "%" ) ) {
      if ( m_operators == null && m_unaryExps == null ) {
        m_operators = new Vector<String>() ;
        m_unaryExps = new Vector<Unary_exp>() ;
      } // if

      m_operators.add( G.s_Context.GetToken() ) ;
      if ( Unary_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        Unary_exp unaryExp = new Unary_exp() ;
        unaryExp.Parse() ;
        m_unaryExps.add( unaryExp ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // while
  } // Parse()
 
  public Node Execute( Node node ) throws Exception {
    Constant constant1 = ( Constant ) node ;
    if ( m_operators == null && m_unaryExps == null )
      ;
    else {
      for ( int i = 0 ; i < m_unaryExps.size() ; i++ ) {
        Constant constant2 = ( Constant ) m_unaryExps.get( i ).Execute() ;
        constant1 = Calculator.Calculate( constant1, m_operators.get( i ), constant2 ) ;
      } // for
    } // else

    return constant1 ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return true ; // could be empty
  } // HeadOf()
} // class Rest_of_maybe_mult_exp implements Node
 
class Unary_exp implements Node {
  Vector<String> m_signs = null ;
  Signed_unary_exp m_signedUnaryExp = null ;
  Vector<String> m_ppOrMm = null ;
  Identifier m_identifier = null ;
  Expression m_expression = null ;
  
  public void Parse() throws Exception {
    if ( Sign.HeadOf( G.s_Context.PeekToken() ) || Signed_unary_exp.HeadOf( G.s_Context.PeekToken() ) ) {
      while ( Sign.HeadOf( G.s_Context.PeekToken() ) ) {
        if ( m_signs == null )
          m_signs = new Vector<String>() ;
        m_signs.add( G.s_Context.GetToken() ) ;
      } // while

      if ( Signed_unary_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        m_signedUnaryExp = new Signed_unary_exp() ;
        m_signedUnaryExp.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // if
    else if ( G.s_Context.PeekToken().equals( "++" ) || G.s_Context.PeekToken().equals( "--" ) ) {
      while ( G.s_Context.PeekToken().equals( "++" ) || G.s_Context.PeekToken().equals( "--" ) ) {
        if ( m_ppOrMm == null )
          m_ppOrMm = new Vector<String>() ;
        m_ppOrMm.add( G.s_Context.GetToken() ) ;
      } // while

      if ( Identifier.HeadOf( G.s_Context.PeekToken() ) ) {
        m_identifier = new Identifier() ;
        m_identifier.Parse() ;
        if ( !Identifier.HasDefined( m_identifier ) )
          Error.Handle( m_identifier.Execute(), Error.UNEXPECTED_TOKEN ) ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      if ( G.s_Context.PeekToken().equals( "[" ) ) {
        G.s_Context.GetToken() ; // skip "["
        if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
          m_expression = new Expression() ;
          m_expression.Parse() ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
        
        if ( G.s_Context.PeekToken().equals( "]" ) )
          G.s_Context.GetToken() ; // skip "]"
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // if
    } // else if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public Node Execute() throws Exception {
    Constant constant = null ;
    if ( m_signedUnaryExp != null ) {
      constant = ( Constant ) m_signedUnaryExp.Execute() ;
      if ( m_signs != null ) {
        for ( int i = 0 ; i < m_signs.size() ; i++ ) {
          if ( m_signs.get( i ).equals( "+" ) )
            constant = Calculator.Calculate( constant, "+", null ) ;
          else if ( m_signs.get( i ).equals( "-" ) )
            constant = Calculator.Calculate( constant, "-", null ) ;
          else // if ( m_signs.get( i ).equals( "!" ) )
            constant = Calculator.Calculate( constant, "!", null ) ;
        } // for
      } // if

      return constant ;
    } // if
    else {
      String name = m_identifier.Execute() ;
      Variable variable = VariableTable.FetchVariable( name ) ;
      if ( m_expression != null ) {
        constant = ( Constant ) m_expression.Execute() ;
        variable = variable.GetAnArrayElement( Integer.valueOf( constant.GetValue() ) ) ;
      } // if

      for ( int i = m_ppOrMm.size() - 1 ; i >= 0 ; i-- ) {
        if ( m_ppOrMm.get( i ).equals( "++" ) )
          variable.SetValue( Calculator.Calculate( variable, "+", new Constant( "1" ) ).GetValue() ) ;
        else // if ( m_ppOrMm.get( i ).equals( "--" ) )
          variable.SetValue( Calculator.Calculate( variable, "-", new Constant( "1" ) ).GetValue() ) ;
      } // for

      constant = new Constant( variable.GetValue() ) ;
      return constant ;
    } // else
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Sign.HeadOf( token ) || Signed_unary_exp.HeadOf( token ) || token.equals( "++" ) ||
           token.equals( "--" ) ;
  } // HeadOf()
} // class Unary_exp implements Node

class Signed_unary_exp implements Node {
  Identifier m_id = null ;
  Rest_of_Identifier_started_unary_exp m_restOfIdentifierStartedUnaryExp = null ;
  Constant m_constant = null ;
  Expression m_expression = null ;
 
  public void Parse() throws Exception {
    if ( Identifier.HeadOf( G.s_Context.PeekToken() ) ) {
      m_id = new Identifier() ;
      m_id.Parse() ;
      if ( !Identifier.HasDefined( m_id ) )
        Error.Handle( m_id.Execute(), Error.UNEXPECTED_TOKEN ) ;
      
      if ( Rest_of_Identifier_started_unary_exp.HeadOf( G.s_Context.PeekToken() ) ) {
        m_restOfIdentifierStartedUnaryExp = new Rest_of_Identifier_started_unary_exp() ;
        m_restOfIdentifierStartedUnaryExp.Parse() ;
      } // if
    } // if
    else if ( Constant.HeadOf( G.s_Context.PeekToken() ) ) {
      m_constant = new Constant() ;
      m_constant.Parse() ;
    } // else if
    else if ( G.s_Context.PeekToken().equals( "(" ) ) {
      G.s_Context.GetToken() ; // skip "("
      if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
        m_expression = new Expression() ;
        m_expression.Parse() ;
      } // if
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      
      if ( G.s_Context.PeekToken().equals( ")" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // else if
    else
      Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
  } // Parse()
 
  public Node Execute() throws Exception {
    if ( m_id != null ) {
      if ( m_restOfIdentifierStartedUnaryExp != null )
        return m_restOfIdentifierStartedUnaryExp.Execute( m_id ) ;
      else {
        Variable var = VariableTable.FetchVariable( m_id.Execute() ) ;
        return var.Copy() ;
      } // else
    } // if
    else if ( m_constant != null )
      return m_constant ;
    else // if ( m_expression != null )
      return m_expression.Execute() ;
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return Identifier.HeadOf( token ) || Constant.HeadOf( token ) || token.equals( "(" ) ;
  } // HeadOf()
} // class Signed_unary_exp implements Node
 
class Rest_of_Identifier_started_unary_exp implements Node {
  Actual_parameter_list m_actualParameterList = null ;
  Expression m_expression = null ;
  String m_ppOrmm = null ;
  // Identifier identifier = null ;
  public void Parse() throws Exception {
    // identifier = id ;
    if ( G.s_Context.PeekToken().equals( "(" ) ) {
      G.s_Context.GetToken() ; // skip "("
      if ( Actual_parameter_list.HeadOf( G.s_Context.PeekToken() ) ) {
        m_actualParameterList = new Actual_parameter_list() ;
        m_actualParameterList.Parse() ;
      } // if
      
      if ( G.s_Context.PeekToken().equals( ")" ) )
        G.s_Context.GetToken() ;
      else
        Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
    } // if
    else {
      if ( G.s_Context.PeekToken().equals( "[" ) ) {
        G.s_Context.GetToken() ; //  skip "["
        if ( Expression.HeadOf( G.s_Context.PeekToken() ) ) {
          m_expression = new Expression() ;
          m_expression.Parse() ;
        } // if
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
        
        if ( G.s_Context.PeekToken().equals( "]" ) )
          G.s_Context.GetToken() ;
        else
          Error.Handle( G.s_Context.GetToken(), Error.UNEXPECTED_TOKEN ) ;
      } // if

      if ( G.s_Context.PeekToken().equals( "++" ) || G.s_Context.PeekToken().equals( "--" ) )
        m_ppOrmm = G.s_Context.GetToken() ;
    } // else
  } // Parse()
 
  public Node Execute( Identifier id ) throws Exception {
    String name = id.Execute() ;
    if ( m_actualParameterList != null ) {
      ArrayList<Node> actualParameterList = m_actualParameterList.Execute() ;
      Function function = FunctionTable.s_allFunction.get( name ) ;
      return function.Call( actualParameterList ) ; // 如果此方選回傳是void,則回傳 null
    } // if
    else {
      Variable var = VariableTable.FetchVariable( name ) ;
      if ( m_expression != null ) {
        Constant constant = ( Constant ) m_expression.Execute() ;
        var = var.GetAnArrayElement( Integer.valueOf( constant.GetValue() ) ) ;
      } // if

      if ( m_ppOrmm != null ) {
        Variable varCopy = var.Copy() ;
        if ( m_ppOrmm.equals( "++" ) )
          var.SetValue( Calculator.Calculate( var, "+", new Constant( "1" ) ).GetValue() ) ;
        else // if ( m_ppOrMm.get( i ).equals( "--" ) )
          var.SetValue( Calculator.Calculate( var, "-", new Constant( "1" ) ).GetValue() ) ;
        return varCopy ;
      } // if

      return var.Copy() ; // 因此方玄只會回傳值，並不是變數本身，為了以防萬一 回傳一個複製版本以免改道原本的值
    } // else
  } // Execute()
 
  public static boolean HeadOf( String token ) {
    return ( token.equals( "(" ) || token.equals( "[" ) || token.equals( "++" ) || token.equals( "--" ) ) ;
  } // HeadOf()
} // class Rest_of_Identifier_started_unary_exp implements Node

class Calculator {
  public static Constant Calculate( Constant c1, String op, Constant c2 ) throws Exception {
    if ( Sign.HeadOf( op ) && c2 == null ) {
      if ( op.equals( "!" ) )
        return new Constant( String.valueOf( !Boolean.valueOf( c1.GetValue() ) ) ) ;
      else {
        float v1 = c1.GetType().equals( "char" ) ? c1.GetValue().charAt( 0 )
                   : c1.GetType().equals( "int" ) ? Integer.valueOf( c1.GetValue() )
                   : Float.valueOf( c1.GetValue() ) ;
        if ( c1.GetType().equals( "int" ) || c1.GetType().equals( "char" ) ) {
          if ( op.equals( "+" ) )
            return new Constant( String.valueOf( ( int ) Math.floor( +v1 ) ) ) ;
          else // if ( op.equals( "-" ) ) 
            return new Constant( String.valueOf( ( int ) Math.floor( -v1 ) ) ) ;
        } // if
        else { // c1.GetType().equals( "float" )
          if ( op.equals( "+" ) )
            return new Constant( String.valueOf( +v1 ) ) ;
          else // if ( op.equals( "-" ) ) 
            return new Constant( String.valueOf( -v1 ) ) ;
        } // else
      } // else
    } // if
    else if ( c1.GetType().equals( "string" ) || c2.GetType().equals( "string" ) ) {
      String v1 = c1.GetValue() ;
      String v2 = c2.GetValue() ;
      if ( op.equals( "+" ) )
        return new Constant( '"' + v1 + v2 + '"' ) ;
      else if ( op.equals( "<" ) )
        return new Constant( String.valueOf( v1.compareTo( v2 ) < 0 ) ) ;
      else if ( op.equals( ">" ) )
        return new Constant( String.valueOf( v1.compareTo( v2 ) > 0 ) ) ;
      else if ( op.equals( "<=" ) )
        return new Constant( String.valueOf( v1.compareTo( v2 ) <= 0 ) ) ;
      else if ( op.equals( ">=" ) )
        return new Constant( String.valueOf( v1.compareTo( v2 ) >= 0 ) ) ;
      else
        return new Constant( '"' + "WTF string" + '"' ) ;
    } // else if
    else if ( c1.GetType().equals( "float" ) || c2.GetType().equals( "float" ) ) {
      float v1 = c1.GetType().equals( "char" ) ? c1.GetValue().charAt( 0 )
                 : c1.GetType().equals( "int" ) ? Integer.valueOf( c1.GetValue() )
                 : Float.valueOf( c1.GetValue() ) ;
      float v2 = c2.GetType().equals( "char" ) ? c2.GetValue().charAt( 0 )
                 : c2.GetType().equals( "int" ) ? Integer.valueOf( c2.GetValue() )
                 : Float.valueOf( c2.GetValue() ) ;

      if ( op.equals( "*" ) )
        return new Constant( String.valueOf( v1 * v2 ) ) ;
      else if ( op.equals( "/" ) )
        return new Constant( String.valueOf( v1 / v2 ) ) ;
      else if ( op.equals( "+" ) )
        return new Constant( String.valueOf( v1 + v2 ) ) ;
      else if ( op.equals( "-" ) )
        return new Constant( String.valueOf( v1 - v2 ) ) ;
      else if ( op.equals( "%" ) )
        return new Constant( String.valueOf( v1 % v2 ) ) ;
      else if ( op.equals( "==" ) )
        return new Constant( String.valueOf( v1 == v2 ) ) ;
      else if ( op.equals( "!=" ) )
        return new Constant( String.valueOf( v1 != v2 ) ) ;
      else if ( op.equals( "<" ) )
        return new Constant( String.valueOf( v1 < v2 ) ) ;
      else if ( op.equals( ">" ) )
        return new Constant( String.valueOf( v1 > v2 ) ) ;
      else if ( op.equals( "<=" ) )
        return new Constant( String.valueOf( v1 <= v2 ) ) ;
      else if ( op.equals( ">=" ) )
        return new Constant( String.valueOf( v1 >= v2 ) ) ;
      else
        return new Constant( '"' + "WTF float" + '"' ) ;
    } // else if
    else if ( ( c1.GetType().equals( "int" ) || c1.GetType().equals( "char" ) ) &&
              ( c2.GetType().equals( "int" ) || c2.GetType().equals( "char" ) ) ) {
      int v1 = c1.GetType().equals( "char" ) ? c1.GetValue().charAt( 0 ) : Integer.valueOf( c1.GetValue() ) ;
      int v2 = c2.GetType().equals( "char" ) ? c2.GetValue().charAt( 0 ) : Integer.valueOf( c2.GetValue() ) ;
      
      if ( op.equals( "*" ) )
        return new Constant( String.valueOf(  v1 * v2 ) ) ;
      else if ( op.equals( "/" ) )
        return new Constant( String.valueOf(  v1 / v2 ) ) ;
      else if ( op.equals( "+" ) )
        return new Constant( String.valueOf( v1 + v2 ) ) ;
      else if ( op.equals( "-" ) )
        return new Constant( String.valueOf( v1 - v2 ) ) ;
      else if ( op.equals( "%" ) )
        return new Constant( String.valueOf( v1 % v2 ) ) ;
      else if ( op.equals( "==" ) )
        return new Constant( String.valueOf( v1 == v2 ) ) ;
      else if ( op.equals( "!=" ) )
        return new Constant( String.valueOf( v1 != v2 ) ) ;
      else if ( op.equals( "<" ) )
        return new Constant( String.valueOf( v1 < v2 ) ) ;
      else if ( op.equals( ">" ) )
        return new Constant( String.valueOf( v1 > v2 ) ) ;
      else if ( op.equals( "<=" ) )
        return new Constant( String.valueOf( v1 <= v2 ) ) ;
      else if ( op.equals( ">=" ) )
        return new Constant( String.valueOf( v1 >= v2 ) ) ;
      else if ( op.equals( "&" ) )
        return new Constant( String.valueOf( v1 & v2 ) ) ;
      else if ( op.equals( "^" ) )
        return new Constant( String.valueOf( v1 ^ v2 ) ) ;
      else if ( op.equals( "|" ) )
        return new Constant( String.valueOf( v1 | v2 ) ) ;
      else
        return new Constant( '"' + "WTF int char" + '"' ) ;
    } // else if
    else if ( c1.GetType().equals( "bool" ) && c2.GetType().equals( "bool" ) ) {
      boolean v1 = Boolean.valueOf( c1.GetValue() ) ;
      boolean v2 = Boolean.valueOf( c2.GetValue() ) ;
      
      if ( op.equals( "||" ) )
        return new Constant( String.valueOf(  v1 || v2 ) ) ;
      else if ( op.equals( "&&" ) )
        return new Constant( String.valueOf( v1 && v2 ) ) ;
      else if ( op.equals( "|" ) )
        return new Constant( String.valueOf( v1 | v2 ) ) ;
      else if ( op.equals( "^" ) )
        return new Constant( String.valueOf( v1 ^ v2 ) ) ;
      else if ( op.equals( "&" ) )
        return new Constant( String.valueOf( v1 & v2 ) ) ;
      else if ( op.equals( "==" ) )
        return new Constant( String.valueOf( v1 == v2 ) ) ;
      else if ( op.equals( "!=" ) )
        return new Constant( String.valueOf( v1 != v2 ) ) ;
      else
        return new Constant( '"' + "WTF bool" + '"' ) ;
    } // else if
     else
       Error.Handle( "line 1 : type error in execute!", Error.UNEXPECTED_TOKEN ) ;
    return null ;
  } // Calculate()
} // class Calculator

  // 將多個字元 組合成一有意義 token  並處裡規範之外的字元

class Context {

  public final static int LETTER = 0 ;
  public final static int DIGIT = 1 ;
  public final static int DOT = 2 ;
  public final static int WHITESPACE = 3 ;
  public final static int COMMENT_LINE = 4 ;
  public final static int UNDERSCORE = 5 ;
  public final static int SINGLE_QUOTE = 6 ;
  public final static int DOUBLE_QUOTE = 7 ;
  public final static int UNKNOWN = 99 ;

  private int m_charClass ;
  private String m_token ;
  private String m_peekToken ;
  private char m_curChar ;
  private char m_nextChar ;
  private String m_inputLine ;

  public Context() {
    m_charClass = WHITESPACE ;
    m_token = new String() ;
    m_peekToken = new String() ;
    m_inputLine = new String() ;
    m_curChar = ' ' ;
    m_nextChar = ' ' ;
  } // Context()

  private char ReadChar() {
    char c = '\0' ;
    if ( m_inputLine.length() == 0 ) {
      m_inputLine = G.s_Input.nextLine() + " \n" ;
      G.s_line++ ;
    } // if

    c = m_inputLine.charAt( 0 ) ;
    m_inputLine = m_inputLine.substring( 1 ) ;
    return c ;
  } // ReadChar()

  private void GetChar() {
    m_curChar = m_nextChar ;
    m_nextChar = ReadChar() ;
    
    if ( Character.isLetter( m_curChar ) )
      m_charClass = LETTER ;
    else if ( Character.isDigit( m_curChar ) )
      m_charClass = DIGIT ;
    else if ( m_curChar == '/' && m_nextChar == '/' )
      m_charClass = COMMENT_LINE ;
    else if ( m_curChar == ' ' || m_curChar == '\n' || m_curChar == '\t' )
      m_charClass = WHITESPACE ;
    else if ( m_curChar == '.' )
      m_charClass = DOT ;
    else if ( m_curChar == '_' )
      m_charClass = UNDERSCORE ;
    else if ( m_curChar == '\'' )
      m_charClass = SINGLE_QUOTE ;
    else if ( m_curChar == '\"' )
      m_charClass = DOUBLE_QUOTE ;
    else // 處裡固定字元數的偷肯
      m_charClass = UNKNOWN ;
  } // GetChar()

  private void AddChar() {
    m_token = m_token + m_curChar ;
  } // AddChar()
  
  private void AddChar( char c ) {
    m_token = m_token + c ;
  } // AddChar()

  void ClearInputLine() {
    m_inputLine = " \n" ;
    m_peekToken = "" ;
    m_token = "" ;
    GetChar() ;
    GetChar() ;
  } // ClearInputLine()

  private void SkipCommentAndSpace() {
    while ( m_charClass == COMMENT_LINE ||  m_charClass == WHITESPACE ) { // m_charClass == COMMENT_BLOCK ||
      if ( m_charClass == COMMENT_LINE ) {
        ClearInputLine() ;
      } // if
      else if ( m_charClass == WHITESPACE )
        GetChar() ;
    } // while

  } // SkipCommentAndSpace()

  public String PeekToken() throws Exception {
    if ( m_peekToken.isEmpty() )
      m_peekToken = GetToken() ;

    return m_peekToken ;
  } // PeekToken()

  public String GetToken() throws Exception {
    // if ( Type_specifier.boolExpressionChecker && Type_specifier.s_tokenCollections.isEmpty() )
    //   Type_specifier.s_tokenCollections.add( m_peekToken ) ;
    
    if ( !m_peekToken.isEmpty() ) {
      m_token = m_peekToken ;
      m_peekToken = new String() ;
      return m_token ;
    } // if
    else {
      m_token = new String() ;
      SkipCommentAndSpace() ;
      // identifier
      if ( m_charClass == LETTER ) {
        AddChar() ;
        GetChar() ;
        while ( m_charClass == LETTER || m_charClass == DIGIT || m_charClass == UNDERSCORE ) {
          AddChar() ;
          GetChar() ;
        } // while
      } // if
      // number
      else if ( m_charClass == DIGIT ) {
        int dotNum = 0 ;
        while ( m_charClass == DIGIT || ( m_charClass == DOT && dotNum < 2 ) ) {
          AddChar() ;
          GetChar() ;
          if ( m_charClass == DOT )
            ++dotNum ;
        } // while
      } // else if
      // float number
      else if ( m_charClass == DOT ) {
        AddChar() ;
        GetChar() ;
        while ( m_charClass == DIGIT ) {
          AddChar() ;
          GetChar() ;
        } // while
      } // else if
      // string
      else if ( m_charClass == DOUBLE_QUOTE ) {
        AddChar() ;
        GetChar() ;
        while ( m_charClass != DOUBLE_QUOTE ) {
          if ( m_curChar == '\\' && m_nextChar == 'n' ) {
            AddChar( '\n' ) ;
            GetChar() ;
            GetChar() ;
          } // if
          else if ( m_curChar == '%' ) {
            AddChar( '%' ) ;
            AddChar( '%' ) ;
            GetChar() ;
          } // else if
          else {
            AddChar() ;
            GetChar() ;
          } // else
        } // while

        AddChar() ;
        GetChar() ;
      } // else if
      // char
      else if ( m_charClass == SINGLE_QUOTE ) {
        AddChar() ;
        GetChar() ;
        AddChar() ;
        GetChar() ;
        AddChar() ;
        GetChar() ;
      } // else if
      // operator
      else if ( m_charClass == UNKNOWN ) {
        Lookup() ;
        GetChar() ;
      } // else if
      // 有的時候要將token收集起來，例如在趴司function時，
      // 要將下面的compound statement收集，之後才能印出來。
      if ( FunctionTable.s_startCollectToken )
        FunctionTable.s_tokenCollections.add( m_token ) ;

      return m_token ;
    } // else
  } // GetToken()

  private boolean IsTwoCharToken() {
    return ( m_curChar == '=' && m_nextChar == '=' ) || ( m_curChar == '>' && m_nextChar == '=' ) ||
           ( m_curChar == '<' && m_nextChar == '=' ) || ( m_curChar == '!' && m_nextChar == '=' ) ||
           ( m_curChar == '|' && m_nextChar == '|' ) || ( m_curChar == '&' && m_nextChar == '&' ) ||
           ( m_curChar == '+' && m_nextChar == '=' ) || ( m_curChar == '-' && m_nextChar == '=' ) ||
           ( m_curChar == '*' && m_nextChar == '=' ) || ( m_curChar == '/' && m_nextChar == '=' ) ||
           ( m_curChar == '%' && m_nextChar == '=' ) || ( m_curChar == '+' && m_nextChar == '+' ) ||
           ( m_curChar == '-' && m_nextChar == '-' ) || ( m_curChar == '<' && m_nextChar == '<' ) ||
           ( m_curChar == '>' && m_nextChar == '>' ) ;
  } // IsTwoCharToken()

  private void Lookup() throws Exception {
    if ( IsTwoCharToken() ) {
      AddChar() ;
      GetChar() ;
      AddChar() ;
    } // if
    else if ( m_curChar == '+' || m_curChar == '-' || m_curChar == '*' || m_curChar == '/' ||
              m_curChar == '=' || m_curChar == '>' || m_curChar == '(' || m_curChar == ')' ||
              m_curChar == '<' || m_curChar == ';' || m_curChar == '{' || m_curChar == '}' ||
              m_curChar == '%' || m_curChar == '&' || m_curChar == '^' || m_curChar == '|' ||
              m_curChar == ',' || m_curChar == '?' || m_curChar == ':' || m_curChar == '[' ||
              m_curChar == ']' || m_curChar == '!' ) {
      AddChar() ;
    } // else if
    else // unrecognized char
      Error.Handle( String.valueOf( m_curChar ), Error.UNRECOGNIZED_CHAR ) ;
  } // Lookup()
} // class Context

class VariableTable {
  public HashMap<String, Variable> m_variables = null ;
  
  public VariableTable() {
    m_variables = new HashMap<String, Variable>() ;
  } // VariableTable()
  
  public VariableTable( VariableTable newTable ) {
    m_variables = new HashMap<String, Variable>() ;
    if ( newTable != null )
      AddVariables( newTable.m_variables ) ;
  } // VariableTable()
  
  public static boolean HasDefined( Identifier id ) throws Exception {
    // 先檢查有沒有在 s_ActivationRecordStack 裡面，當然，如果目前不是在定義方選，s_ActivationRecordStack 會是 empty
    // 之後再檢查有沒有在 s_variableTables 裡面，依照目前 s_scope 值，遞減檢查
    // 若都沒有那就是變數還沒有宣告。
    String name = id.Execute() ;
   
    if ( !G.s_ActivationRecordStack.empty() && G.s_ActivationRecordStack.peek().m_arguments != null ) {
      Argument[] args = G.s_ActivationRecordStack.peek().m_arguments ;
      for ( int i = 0; i < args.length && args[i] != null ; i++ ) {
        Argument arg = args[i] ;
        if ( arg.m_name.equals( name ) )
          return true ;
      } // for
    } // if

    if ( name.equals( "cin" ) || name.equals( "cout" ) )
      return true ;

    // To check local variable
    if ( !G.s_allVariableTablesList.empty() ) {
      for ( int i = 0 ; i < G.s_allVariableTablesList.peek().size() ; i++ ) {
        if ( G.s_allVariableTablesList.peek().get( i ).m_variables.containsKey( name )  )
          return true ;
      } // for
    } // if
    
    // To check global variable
    if ( G.s_globalVariableTable.m_variables.containsKey( name ) )
      return true ;

    return false ;
  } // HasDefined()

  public void AddVariables( HashMap<String, Variable> newVariables ) {
    if ( newVariables != null ) {
      ArrayList<String> nameList = new ArrayList<String>( newVariables.keySet() ) ;
      for ( int i = 0 ; i < nameList.size() ; i++ ) {
        String name = nameList.get( i ) ;
        m_variables.put( name, newVariables.get( name ).Copy() ) ;
      } // for
    } // if
  } // AddVariables()
  
  public VariableTable Copy() {
    VariableTable table = new VariableTable() ;
    ArrayList<String> nameList = new ArrayList<String>( m_variables.keySet() ) ;
    for ( int i = 0 ; i < nameList.size() ; i++ ) {
      String name = nameList.get( i ) ;
      table.m_variables.put( name, this.m_variables.get( name ).Copy() ) ;
    } // for

    return table ;
  } // Copy()
  
  public static Variable FetchVariable( String name ) {
    // fetch local variable
    for ( int i = G.s_scope ; i >= 1 ; i-- ) {
      if ( G.s_allVariableTablesList.peek().get( i ).m_variables.containsKey( name ) )
        return G.s_allVariableTablesList.peek().get( i ).m_variables.get( name ) ;
    } // for
    // fetch global variable
    return G.s_globalVariableTable.m_variables.get( name ) ;
  } // FetchVariable()
} // class VariableTable

class Variable extends Constant {
  String m_name = null ;
  ArrayList<Variable> m_array = null ; // if the var is not array, m_array is null.
  int m_arraySize = 0 ;

  public Variable( String name, String value, String type, String size ) {
    m_name = name ;
    SetType( type ) ;
    SetValue( value ) ;
    if ( size != null ) {
      m_arraySize = Integer.valueOf( size ) ;
      m_array = new ArrayList<Variable>( m_arraySize ) ;
      for ( int i = 0 ; i < m_arraySize ; i++ ) // initial array.
        m_array.add( new Variable( m_name, null, type, null ) ) ;
    } // if
  } // Variable()
  
  public void SetType( String type ) {
    if ( type.equals( "string" ) )
      m_type = Type_specifier.STRING ;
    else if ( type.equals( "float" ) )
      m_type = Type_specifier.FLOAT ;
    else if ( type.equals( "char" ) )
      m_type = Type_specifier.CHAR ;
    else if ( type.equals( "bool" ) )
      m_type = Type_specifier.BOOL ;
    else
      m_type = Type_specifier.INT ;
  } // SetType()
  
  public void SetValue( String value ) {
    if ( value != null ) {
      if ( m_type == Type_specifier.INT && value.contains( "." ) )
        m_value = value.substring( 0, value.indexOf( '.' ) ) ;  
      else
        m_value = new String( value ) ;
    } // if
    else {
      if ( m_type == Type_specifier.CHAR || m_type == Type_specifier.STRING )
        m_value = "\0" ;
      else if ( m_type == Type_specifier.INT )
        m_value = "0" ;
      else if ( m_type == Type_specifier.FLOAT )
        m_value = "0.0" ;
      else // bool
        m_value = "true" ;
    } // else
  } // SetValue()
  
  public Variable GetAnArrayElement( int index ) {
    if ( m_array != null && index < m_arraySize )
      return m_array.get( index ) ;
    else
      return null ;
  } // GetAnArrayElement()
  
  public Variable Copy() {
    Variable var = null ;
    var = new Variable( m_name, m_value, this.GetType(),
                        m_arraySize == 0 ? null : String.valueOf( m_arraySize ) ) ;
    if ( var.m_arraySize != 0 ) {
      for ( int i = 0 ; i < m_arraySize ; i++ )
        var.m_array.add( i, this.m_array.get( i ).Copy() ) ;
    } // if
    
    return var ;
  } // Copy()
  
  public int GetArraySize() {
    return m_arraySize ;
  } // GetArraySize()
} // class Variable

class ReturnException extends Exception {

  public String m_value = null ;
  
  public ReturnException( String v ) {
    super( v ) ;
    m_value = v ;
  } // ReturnException()
  
  public static void Handle( String v ) throws ReturnException {
    throw new ReturnException( v ) ;
  } // Handle()
} // class ReturnException

class FunctionTable {
  public static HashMap<String, Function> s_allFunction = new HashMap<String, Function>() ;
  // 以下的變數是為了ListFunction() 而準備，將方玄內的statement存起來
  public static HashMap<String, Vector<String> > s_allFunctionStmt = new HashMap<String, Vector<String> >() ;
  public static Vector<String> s_tokenCollections = new Vector<String>() ;
  public static boolean s_startCollectToken = false ;
 
  public static void RunSystemSupportFunction( Identifier id ) throws Exception {
    String name = id.Execute() ;
    if ( name.equals( "ListAllVariables" ) )
      ListAllVariables() ;
    else if ( name.equals( "ListAllFunctions" ) )
      ListAllFunctions() ;
    else if ( name.equals( "ListVariable" ) )
      ListVariable() ;
    else if ( name.equals( "ListFunction" ) )
      ListFunction() ;
    else { // if ( name.equals( "Done" ) ) {
      G.s_Context.GetToken() ; // skip "("
      G.s_Context.GetToken() ; // skip ")"
      G.s_running = false ;
    } // else if
  } // RunSystemSupportFunction()
  
  public static boolean IsSystemSupportFunction( String name ) {
    return  name.equals( "ListAllVariables" ) || name.equals( "ListAllFunctions" ) ||
            name.equals( "ListVariable" ) || name.equals( "ListFunction" ) || name.equals( "Done" ) ;
  } // IsSystemSupportFunction()
  
  public static void ListAllVariables() throws Exception {
    G.s_Context.GetToken() ; // skip "("
    ArrayList<String> list = null ;
    list = new ArrayList<String>( G.s_globalVariableTable.m_variables.keySet() ) ;
    Collections.sort( list ) ;
    for ( int i = 0 ; i < list.size() ; i++ )
      System.out.println( list.get( i ) ) ;
    G.s_Context.GetToken() ; // skip ")"
  } // ListAllVariables()
  
  public static void ListVariable() throws Exception {
    G.s_Context.GetToken() ; // skip "("
    String name = G.s_Context.GetToken() ;
    Variable variable = G.s_globalVariableTable.m_variables.get( name.substring( 1, name.length() - 1 ) ) ;
    System.out.printf( variable.GetType() + " " + variable.m_name ) ;
    if ( variable.m_array != null )
      System.out.printf( "[ " + variable.m_array.size() + " ]" ) ;
    System.out.printf( " ;\n" ) ;
    G.s_Context.GetToken() ; // skip ")"
  } // ListVariable()
  
  public static void ListAllFunctions() throws Exception {
    G.s_Context.GetToken() ; // skip "("
    ArrayList<String> list = new ArrayList<String>( FunctionTable.s_allFunction.keySet() ) ;
    Collections.sort( list ) ;
    for ( int i = 0 ; i < list.size() ; i++ )
      System.out.println( list.get( i ) + "()" ) ;
    G.s_Context.GetToken() ; // skip ")"
  } // ListAllFunctions()
  
  public static void ListFunction() throws Exception {
    G.s_Context.GetToken() ; // skip "("
    String name = G.s_Context.GetToken() ;
    Function function = FunctionTable.s_allFunction.get( name.substring( 1, name.length() - 1 ) ) ;
    Argument[] args = function.m_args ;
    Vector<String> allStmt = FunctionTable.s_allFunctionStmt.get( function.m_name ) ;
    int indentCount = 0 ;
    if ( function.m_returnType == null )
      System.out.printf( "void " ) ;
    else
      System.out.printf( function.m_returnType + " " ) ;
    System.out.printf( function.m_name + "( " ) ;
    for ( int i = 0 ; i < args.length && args[i] != null ; i++ ) {
      System.out.printf( args[i].m_type + " " ) ;
      System.out.printf( args[i].m_callByReference ? "& " : "" ) ;
      System.out.printf( args[i].m_name +
                         ( ( args[i].m_arraySize != -1 )
                           ? "[ " + String.valueOf( args[i].m_arraySize ) + " ]" : "" ) ) ;
      System.out.printf( ( i != args.length - 1 && args[i + 1] != null ) ? ", " : " " ) ;      
    } // for

    System.out.printf( ") " ) ;
    for ( int i = 0 ; i < allStmt.size() ; i++ ) {
      String token = allStmt.get( i ) ;
      String nextToken = i + 1 != allStmt.size() ? allStmt.get( i+1 ) : "" ;
      if ( token.equals( "{" ) ) {
        System.out.printf( "{\n" ) ;
        indentCount = indentCount + 2 ;
        PrintIndent( indentCount, nextToken ) ;
      } // if
      else if ( token.equals( "}" ) ) {
        indentCount = indentCount - 2 ;
        System.out.printf( "}\n" ) ;
        PrintIndent( indentCount, nextToken ) ;
      } // else if
      else if ( token.equals( ";" ) ) {
        System.out.printf( ";\n" ) ;
        PrintIndent( indentCount, nextToken ) ;
      } // else if
      else if ( ( token.equals( "++" ) || token.equals( "--" ) ) && Identifier.HeadOf( nextToken ) )
        System.out.printf( token ) ;
      else if ( Identifier.HeadOf( token ) && ( nextToken.equals( "++" ) || nextToken.equals( "--" ) ) )
        System.out.printf( token ) ;
      else if ( nextToken.equals( "[" ) || nextToken.equals( "," ) )
        System.out.printf( token ) ;
      else if ( Identifier.HeadOf( token ) && nextToken.equals( "(" ) )
        System.out.printf( token ) ;
      else
        System.out.printf( ( token.equals( "%" ) ? "%%" : token ) + " " ) ;
    } // for

    G.s_Context.GetToken() ; // skip ")"
  } // ListFunction()
  
  private static void PrintIndent( int indentCount, String nextToken ) {
    if ( nextToken.equals( "}" ) ) {
      for ( int j = 0 ; j < indentCount - 2 ; j++ ) // 印縮排
        System.out.printf( " " ) ;
    } // if
    else {
      for ( int j = 0 ; j < indentCount ; j++ ) // 印縮排
        System.out.printf( " " ) ;
    } // else
  } // PrintIndent()
} // class FunctionTable

class Function {
  String m_name = null ;
  Argument[] m_args = null ;
  String m_returnType = null ;
  Compound_statement m_compoundStatement = null ;
  
  public Function( String n, Argument[] a, String t, Compound_statement c ) {
    m_name = n ;
    m_args = a ;
    m_returnType = t ; // 若 m_returnType 依然是  null, 表示為 void
    m_compoundStatement = c ;
  } // Function()

  // Function call 應注意事項:
  // 0. call 之前、之後，都要有相對應處理
  // 1. 要把 Argument 丟進去 Activation Record 裡
  // 2. 裡面可能會出現 return ，或許可以用例外處理方式來解決?
  public Node Call( ArrayList<Node> actualParameterList ) throws Exception {
    // String value = null ;
    // if ( m_compoundStatement != null ) {
    //   try {
    //     G.s_ActivationRecordStack.push( new ActivationRecord( m_name, m_args ) ) ;
    //     G.s_scope++ ;
    //     m_compoundStatement.Execute() ;
    //   } // try
    //   catch ( ReturnException returnException ) {
    //     value = returnException.value ;
    //   } // catch
    //   finally {
    //     G.s_scope-- ;
    //     G.s_ActivationRecordStack.pop() ;
    //   } // finally
    // } // if
    // return new Constant( value ) ;
    return null ;
  } // Call()
} // class Function

class Argument {
  String m_type = null ;
  boolean m_callByReference = false ;
  String m_name = null ;
  int m_arraySize = -1 ;
  
  public Argument( String t, boolean ref, String ident, String arr_size ) {
    m_type = t ;
    m_callByReference = ref ;
    m_name = ident ;
    m_arraySize = arr_size == null ? -1 : Integer.valueOf( arr_size ) ;
  } // Argument()
} // class Argument

class ActivationRecord {
  String m_functionName = null ;
  Argument[] m_arguments = null ;
  
  public ActivationRecord( String name, Argument[] args ) {
    m_functionName = name ;
    m_arguments = args ;
  } // ActivationRecord()
} // class ActivationRecord

class ReserveWord {
  public static boolean Check( String token ) {
    return token.equals( "int" ) || token.equals( "float" ) || token.equals( "char" ) ||
           token.equals( "bool" ) || token.equals( "string" ) || token.equals( "void" ) ||
           token.equals( "if" ) || token.equals( "else" ) || token.equals( "while" ) ||
           token.equals( "do" ) || token.equals( "return" ) || token.equals( "true" ) ||
           token.equals( "false" ) ;
  } // Check() ;
} // class ReserveWord

class Error {
  public static boolean s_theLineErrorInWhichNoElseThere = false ;
  public static int s_curTokenLine = 0 ;
  public static int s_nextTokenLine = 0 ;
  // error type
  public final static int UNEXPECTED_TOKEN = 0 ;
  public final static int UNRECOGNIZED_CHAR = 1 ;

  public static void Handle( String error, int type ) throws Exception {
    G.s_line = G.s_line == 0 ? 1 : G.s_line ;
    if ( type == UNEXPECTED_TOKEN )
      throw new Exception( "line " + G.s_line + " : syntax error when token is '" + error + "'" ) ;
    else if ( type == UNRECOGNIZED_CHAR )
      throw new Exception( "line " + G.s_line + " : syntax error when input char is '" + error + "'" ) ;
    else // if ( type == TYPE_ERROR )
      throw new Exception( "line " + G.s_line + " : type error!" ) ;
  } // Handle()
} // class Error

class G {
  public final static int GLOBAL_SCOPE = 0 ;
  public static Scanner s_Input = new Scanner( System.in ) ;
  public static Context s_Context = new Context() ;
  public static Stack< ArrayList<VariableTable>> s_allVariableTablesList = new Stack<ArrayList<VariableTable>>() ;
  public static VariableTable s_globalVariableTable = new VariableTable() ;
  public static int s_scope = GLOBAL_SCOPE ;
  public static int s_line = 0 ; // 紀錄目前讀到第幾行
  public static boolean s_running = true ;
  public static Stack<ActivationRecord> s_ActivationRecordStack = new Stack<ActivationRecord>() ;
} // class G

class Main {
  public static void main( String[] args ) throws Throwable {
    System.out.println( "Our-C running ..." ) ;
    // G.s_Input.nextLine() ; // 題數
    User_Input userInput = null ;

    while ( G.s_running ) {
      System.out.printf( "> " ) ;
      userInput = new User_Input() ;
      try {
        userInput.Parse() ;
        userInput.Execute() ;
      } // try
      catch ( Exception error ) {
        System.out.println( error.getMessage() ) ;
        // error.printStackTrace() ;
        G.s_Context.ClearInputLine() ;
      } // catch
      finally {
        G.s_scope = G.GLOBAL_SCOPE ;
        G.s_line = 0 ;
        userInput = null ;
        if ( Error.s_theLineErrorInWhichNoElseThere ) {
          G.s_line = Error.s_nextTokenLine - Error.s_curTokenLine ;
          Error.s_theLineErrorInWhichNoElseThere = false ;
        } // if
        // if ( Type_specifier.boolExpressionChecker ) {
          // Type_specifier.boolExpressionChecker = false ;
          // Type_specifier.s_tokenCollections = new Vector<String>() ;
        // } // if
      } // finally
    } // while

    System.out.printf( "Our-C exited ..." ) ;
  } // main()
} // class Main
