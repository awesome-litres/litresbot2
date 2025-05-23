package litresbot.books.plurals;

import litresbot.books.plurals.Plurals.PluralForm;

public final class PluralsTextEn {
    private PluralsTextEn() {
    } // never

    public static String convert(String text, int n) {
        String resultText = text;
        PluralForm pluralForm = PluralForm.TWO;

        if (n == 1) {
            pluralForm = PluralForm.ONE;
        }

        if (text.equals("book")) {
            switch (pluralForm) {
                case ONE:
                    resultText = "book";
                    break;

                case TWO:
                    resultText = "books";
                    break;

                default:
                    break;
            }

            return resultText;
        }

        return resultText;
    }
}
