/*************************************************************************
 * Copyright 2009-2015 Eucalyptus Systems, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Please contact Eucalyptus Systems, Inc., 6755 Hollister Ave., Goleta
 * CA 93117, USA or visit http://www.eucalyptus.com/licenses/ if you need
 * additional information or have any questions.
 ************************************************************************/
package com.eucalyptus.util;

import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Utility functions for strings.
 */
public class Strings {

  /**
   * Null safe string conversion
   *
   * @param object The object to convert to a String
   * @return The object as a String or null if null
   */
  public static String toString( @Nullable final Object object ) {
    return Objects.toString( object, null );
  }

  /**
   * Remove optional prefix from the given text.
   *
   * @param text The text to trim
   * @return The trimmed text or null if text was null
   */
  public static String trimPrefix( @Nonnull  final String prefix,
                                   @Nullable final String text ) {
    if ( text != null && text.startsWith( prefix ) ) {
      return text.substring( prefix.length( ) );
    } else {
      return text;
    }
  }

  /**
   * Remove optional suffix from the given text.
   *
   * @param text The text to trim
   * @return The trimmed text or null if text was null
   */
  public static String trimSuffix( @Nonnull  final String suffix,
                                   @Nullable final String text ) {
    if ( text != null && text.endsWith( suffix ) ) {
      return text.substring( 0, text.length( ) - suffix.length( ) );
    } else {
      return text;
    }
  }

  /**
   * Get a Function for trimming a String.
   *
   * <P>The returned function will pass through null values.</P>
   *
   * @return The trimming function
   * @see String#trim()
   */
  public static Function<String,String> trim() {
    return StringFunctions.TRIM;
  }

  /**
   * Get the substring of text that precedes the match, empty if not found.
   *
   * @param match The boundary string to search for
   * @param text The text to process
   * @return The substring, null if text was null
   */
  public static String substringBefore( @Nonnull  final String match,
                                        @Nullable final String text ) {
    if ( text != null ) {
      final int index = text.indexOf( match );
      if ( index < 0 ) {
        return "";
      } else {
        return text.substring( 0, index );
      }
    } else {
      return null;
    }
  }

  /**
   * Get a function returning the substring of text that precedes the match, empty if not found.
   *
   * @param match The boundary string to search for
   * @return The substring function, returns null if text was null
   */
  public static Function<String,String> substringBefore( @Nonnull final String match ) {
    return new Function<String,String>( ) {
      @Nullable
      @Override
      public String apply( @Nullable final String text ) {
        return substringBefore( match, text );
      }
    };
  }

  /**
   * Get the substring of text that follows the match, empty if not found.
   *
   * @param match The boundary string to search for
   * @param text The text to process
   * @return The substring, null if text was null
   */
  public static String substringAfter( @Nonnull  final String match,
                                       @Nullable final String text ) {
    if ( text != null ) {
      final int index = text.indexOf( match );
      if ( index < 0 ) {
        return "";
      } else {
        return text.substring( index + match.length( ) );
      }
    } else {
      return null;
    }
  }

  /**
   * Get a function returning the substring of text that follows the match, empty if not found.
   *
   * @param match The boundary string to search for
   * @return The substring function, returns null if text was null
   */
  public static Function<String,String> substringAfter( @Nonnull  final String match ) {
    return new Function<String,String>( ) {
      @Nullable
      @Override
      public String apply( @Nullable final String text ) {
        return substringAfter( match, text );
      }
    };
  }

  /**
   * Get a Function for upper casing a String.
   *
   * <P>The returned function will pass through null values.</P>
   *
   * @return The upper casing function
   * @see String#toUpperCase()
   */
  public static Function<String,String> upper() {
    return StringFunctions.UPPER;
  }

  /**
   * Get a Function for lower casing a String.
   *
   * <P>The returned function will pass through null values.</P>
   *
   * @return The upper casing function
   * @see String#toLowerCase() ()
   */
  public static Function<String,String> lower() {
    return StringFunctions.LOWER;
  }

  /**
   * Get a Predicate for matching the start of a String.
   *
   * @param prefix The prefix to match
   * @return The predicate
   * @see String#startsWith(String)
   */
  public static Predicate<String> startsWith( final String prefix ) {
    return new Predicate<String>() {
      @Override
      public boolean apply( @Nullable final String text ) {
        return text != null && text.startsWith( prefix );
      }
    };
  }

  /**
   * Get a Predicate for matching the start of a String.
   *
   * @param text The text to perform a prefix match against
   * @return The predicate
   * @see String#startsWith(String)
   */
  public static Predicate<String> isPrefixOf( final String text ) {
    return text == null ?
        Predicates.<String>alwaysFalse() :
        new Predicate<String>() {
      @Override
      public boolean apply( @Nullable final String prefix ) {
        return prefix != null && text.startsWith( prefix );
      }
    };
  }

  /**
   * Get a Predicate for matching the end of a String.
   *
   * @param suffix The suffix to match
   * @return The predicate
   * @see String#endsWith(String)
   */
  public static Predicate<String> endsWith( final String suffix ) {
    return new Predicate<String>() {
      @Override
      public boolean apply( @Nullable final String text ) {
        return text != null && text.endsWith( suffix );
      }
    };
  }

  /**
   * Get a Predicate for matching the end of a String.
   *
   * @param text The text to perform a suffix match against
   * @return The predicate
   * @see String#endsWith(String)
   */
  public static Predicate<String> isSuffixOf( final String text ) {
    return text == null ?
        Predicates.<String>alwaysFalse() :
        new Predicate<String>() {
          @Override
          public boolean apply( @Nullable final String prefix ) {
            return prefix != null && text.endsWith( prefix );
          }
        };
  }

  /**
   * Get a Function that appends the given text to it's parameter.
   *
   * @param suffix The suffix to append.
   * @return The function
   */
  public static Function<String,String> append( final String suffix ) {
    return new Function<String, String>( ) {
      @Nullable
      @Override
      public String apply( @Nullable final String text ) {
        return text == null ?
            suffix :
            text + suffix;
      }
    };
  }

  /**
   * Get a Function that prepends the given text to it's parameter.
   *
   * @param prefix The prefix to prepend.
   * @return The function
   */
  public static Function<String,String> prepend( final String prefix ) {
    return new Function<String, String>( ) {
      @Nullable
      @Override
      public String apply( @Nullable final String text ) {
        return text == null ?
            prefix :
            prefix + text;
      }
    };
  }

  public static Function<String,Function<String,String>> join( ) {
    return new Function<String,Function<String,String>>( ) {
      @Nullable
      @Override
      public Function<String,String> apply( @Nullable final String prefix ) {
        return prepend( prefix );
      }
    };
  }

  /**
   *
   * @see java.util.regex.Matcher#quoteReplacement(String)
   */
  public static Function<String,String> regexReplace(
      final Pattern pattern,
      final String replacement,
      final String defaultValue
  ) {
    return new Function<String,String>( ) {
      @Nullable
      @Override
      public String apply( @Nullable final String text ) {
        if ( text == null ) {
          return defaultValue;
        } else {
          final Matcher matcher = pattern.matcher( text );
          return matcher.matches( ) ?
              matcher.replaceFirst( replacement ) :
              defaultValue;
        }
      }
    };
  }

  /**
   * Convert an object to a string.
   *
   * <P>The returned function will pass through null values.</P>
   * 
   * @return The toString function
   * @see #toString(Object)
   */
  public static Function<Object,String> toStringFunction() {
    return StringerFunctions.TOSTRING;
  }

  /**
   * Get a CharSequence that is the concatenation of the given sequences.
   *
   * @param sequences The sequences to concatenate
   * @return The new sequence
   */
  public static CharSequence concat( final Iterable<? extends CharSequence> sequences ) {
    return concat( sequences, 0, length( sequences ) );
  }

  /**
   * Get a CharSequence that is the concatenation of the given sequences.
   *
   * @param sequences The sequences to concatenate
   * @param start The start of the sequence
   * @param end The end of the sequence
   * @return The new sequence
   */
  public static CharSequence concat( final Iterable<? extends CharSequence> sequences, final int start, final int end ){
    final int sequencesLength = end - start;
    return new CharSequence( ) {
      @Override
      public int length( ) {
        return sequencesLength;
      }

      @Override
      public char charAt( final int index ) {
        if ( index < 0 || index >= length( ) ) {
          throw new IndexOutOfBoundsException( String.valueOf( index ) );
        }
        int adjustedIndex = start + index;
        for ( final CharSequence sequence : sequences ) {
          if ( adjustedIndex < sequence.length( ) ) {
            return sequence.charAt( adjustedIndex );
          } else {
            adjustedIndex -= sequence.length( );
          }
        }
        throw new IndexOutOfBoundsException( String.valueOf( index ) );
      }

      @Override
      public CharSequence subSequence( final int start, final int end ) {
        return concat( Collections.singleton( this ), start, end );
      }

      @Nonnull
      @Override
      public String toString( ) {
        return new StringBuilder( this ).toString( );
      }
    };
  }

  private static int length( final Iterable<? extends CharSequence> sequences ) {
    int length = 0;
    for ( final CharSequence sequence : sequences ) {
      length += sequence.length( );
    }
    return length;
  }

  private enum StringFunctions implements Function<String,String> {
    LOWER {
      @Override
      public String apply( final String text ) {
        return text == null ? null : text.toLowerCase();
      }
    },
    UPPER {
      @Override
      public String apply( final String text ) {
        return text == null ? null : text.toUpperCase();
      }
    },
    TRIM {
      @Override
      public String apply( final String text ) {
        return text == null ? null : text.trim();
      }
    }
  }
  
  private enum StringerFunctions implements Function<Object,String> {
    TOSTRING {
      @Override
      public String apply( final Object object ) {
        return Strings.toString( object );
      }
    }
  }
}
