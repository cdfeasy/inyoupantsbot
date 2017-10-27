package idgtl

class Texts{
    fun isVerb(word:String, isRus: Boolean): Boolean{
        if(isRus){
            return (word.endsWith("ай") || word.endsWith("уй"))&& !word.equals("хуй");
        }else{
            return false;
        }
    }

    fun prefix1(isRus: Boolean):String{
        return if(isRus) prefix1Rus else prefix1Eng;
    }
    fun prefix2(isRus: Boolean):String{
        return if(isRus) prefix2Rus else prefix2Eng;
    }
    fun postfix1(isRus: Boolean):String{
        return if(isRus) postfix1Rus else postfix1Eng;
    }
    fun postfix2(isRus: Boolean):String{
        return if(isRus) postfix2Rus else postfix2Eng;
    }
    fun verbPrefix1(isRus: Boolean):String{
        return if(isRus) verbPrefix1Rus else verbPrefix1Eng;
    }
    fun verbPostfix1(isRus: Boolean):String{
        return if(isRus) verbPostfix1Rus else verbPostfix1Eng;
    }

    fun dog1(isRus: Boolean):String{
        return if(isRus) dog1Rus else dog1Eng;
    }
    fun dog2(isRus: Boolean):String{
        return if(isRus) dog2Rus else dog2Eng;
    }


    private val prefix1Rus = "В штанах твоих ";
    private val prefix2Rus = "В штанах у тебя ";
    private var postfix1Rus = " у тебя в штанах";
    private var postfix2Rus = " у тебя в штанах, пёс";
    private val verbPrefix1Rus = "Штаны себе сперва "
    private val verbPostfix1Rus = " в штаны себе, пёс"


    private val dog1Rus = "пёс";
    private val dog2Rus = "сутулый пёс";

    private val dog1Eng = "dog";
    private val dog2Eng = "dog";


    private val prefix1Eng = "In your pants ";
    private val prefix2Eng  = "In your pants ";
    private var postfix1Eng  = " in your pants";
    private var postfix2Eng = " in your pants, dog";
    private val verbPrefix1Eng  = "Штаны себе сперва "
    private val verbPostfix1Eng  = " в штаны себе, пёс"

}