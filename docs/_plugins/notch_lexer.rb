# Rouge lexer for Notch. Use with fenced code blocks tagged `notch`:
#
#   ```notch
#   for x in [1, 2, 3]
#     print(x)
#   end
#   ```
#
# Token classes map to the existing Flexoki theme in _sass/_syntax.scss
# (.k, .s1, .s2, .mi, .nf, .nc, .o, .c1, etc).

require 'rouge'

module Rouge
  module Lexers
    class Notch < RegexLexer
      title 'Notch'
      desc 'A scripting language for the JVM'
      tag 'notch'
      filenames '*.notch'

      KEYWORDS = %w[
        if elseif else end
        for in while do
        return break continue
        import as
        fn def let var
        try catch throw finally
        class extends
      ].freeze

      KEYWORDS_CONSTANT = %w[true false null].freeze

      OPERATORS_WORD = %w[and or not].freeze

      BUILTINS = %w[print].freeze

      state :root do
        rule %r/\s+/, Text::Whitespace

        rule %r{//.*?$}, Comment::Single
        rule %r{\#.*?$}, Comment::Single
        rule %r{/\*}, Comment::Multiline, :comment_block

        rule %r/'(\\.|[^'\\])*'/, Str::Single
        rule %r/"(\\.|[^"\\])*"/, Str::Double

        rule %r/0x[0-9a-fA-F_]+/, Num::Hex
        rule %r/0b[01_]+/,        Num::Bin
        rule %r/0o[0-7_]+/,       Num::Oct
        rule %r/\d+\.\d+([eE][+-]?\d+)?/, Num::Float
        rule %r/\d+/,             Num::Integer

        rule %r/[+\-*\/%]=?/, Operator
        rule %r/[=!<>]=?/,    Operator
        rule %r/&&|\|\||!|&|\|/, Operator
        rule %r/[.,;:?]/,     Punctuation
        rule %r/[\[\]{}()]/,  Punctuation

        rule %r/[A-Za-z_][A-Za-z0-9_]*/ do |m|
          text = m[0]
          if KEYWORDS.include?(text)
            token Keyword
          elsif KEYWORDS_CONSTANT.include?(text)
            token Keyword::Constant
          elsif OPERATORS_WORD.include?(text)
            token Operator::Word
          elsif BUILTINS.include?(text)
            token Name::Builtin
          elsif text =~ /\A[A-Z]/
            token Name::Class
          else
            token Name
          end
        end
      end

      state :comment_block do
        rule %r{[^*\/]+}, Comment::Multiline
        rule %r{/\*},     Comment::Multiline, :comment_block
        rule %r{\*/},     Comment::Multiline, :pop!
        rule %r{[*\/]},   Comment::Multiline
      end
    end
  end
end
