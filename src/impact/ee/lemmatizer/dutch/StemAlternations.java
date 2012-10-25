package impact.ee.lemmatizer.dutch;

/**
 * Rather ugly to include data in this way, should be in resources.
 * @author does
 *
 */
public class StemAlternations 
{
	public static final String[][] strongVerbs =
		{
			{"aansluiken", "aangesloken", "aansluiken", "aansloken",  "aanslook"},
			{"bakken", "bak", "bakken", "bakt", "bakte", "bakten", "biek", "bieken", "gebakken"},
			{"bannen", "ban", "bande", "banden", "bannen", "bant", "gebannen"},
			{"barsten", "barst", "barsten", "barstte", "barstten", "borst", "borsten", "gebarsten", "geborsten"},
			{"bassen", "bas", "bassen", "bast", "baste", "basten", "gebassen", "gebast"},
			{"beginnen", "began", "beginnen", "begoest", "begoesten", "begon", "begonde", "begonden", "begonnen", "begonst", "begonste", "begonsten", "begost", "begosten"},
			{"beklijven", "bekleef", "bekleven", "beklijven"},
			{"belanden", "belanden", "belonden", "belonnen"},
			{"belgen", "belgen", "gebolgen"},
			{"bereiken", "bereek", "bereiken", "bereken"},
			{"bergen", "berg", "bergen", "bergt", "borg", "borgen", "geborgen"},
			{"beseffen", "beseffen", "beseven", "besief", "besof"},
			{"bevelen", "beval", "bevalen", "bevelen"},
			{"bevelen", "bevolen"},
			{"bezijpen", "bezepen", "bezijpen"},
			{"bezwijken", "bezweek", "bezweken", "bezwijken"},
			{"bidden", "bad", "baden", "bid", "bidden", "bidt", "gebeden"},
			{"bieden", "bied", "bieden", "biedt", "boden", "bood", "geboden"},
			{"bijten", "beet", "beten", "bijt", "bijten", "gebeten"},
			{"bijten", "beet", "beten", "bijt", "bijten", "gebeten"},
			{"bijzen", "bees", "bezen", "bijzen", "gebezen"},
			{"binden", "bind", "binden", "bindt", "bond", "bonden", "gebonden", "gebongen"},
			{"blazen", "blaas", "blaast", "blazen", "blies", "bliezen", "bloes", "bloezen", "geblazen"},
			{"blijken", "bleek", "bleken", "blijk", "blijken", "blijkt", "gebleken"},
			{"blijven", "bleef", "bleven", "blijf", "blijft", "blijve", "blijven", "blééf", "gebleven"},
			{"blinken", "blink", "blinken", "blinkt", "blinkte", "blinkten", "blonk", "blonken", "geblonken"},
			{"boten", "bootte", "bootten", "geboot", "geboten"},
			{"bouwen", "bouw", "bouwde", "bouwden", "bouwen", "bouwt", "gebouwd", "gebouwen"},
			{"braden", "braad", "braadde", "braadden", "braadt", "braden", "bried", "brieden", "gebraan", "gebraden"},
			{"breken", "brak", "braken", "breek", "breekt", "breken", "broken", "gebroken"},
			{"brengen", "bracht", "brachten", "breng", "brengen", "brengt", "brocht", "brochten", "brong", "brongen", "gebracht", "gebrocht", "gebrongen"},
			{"brouwen", "brieu", "brieuen", "brouw", "brouwde", "brouwden", "brouwen", "brouwt", "gebrouwd", "gebrouwen"},
			{"buigen", "bogen", "boog", "buig", "buigen", "buigt", "gebogen"},
			{"delven", "delf", "delfde", "delfden", "delft", "delven", "dolf", "dolven", "gedolven"},
			{"denken", "dacht", "dachten", "denken", "docht", "dochten", "gedacht", "gedocht"},
			{"derven", "derf", "derfde", "derfden", "derft", "derven", "dierf", "dierven", "gederfd", "gedorven"},
			{"dijen", "deeg", "degen", "dij", "dijde", "dijden", "dijen", "dijt", "gedegen", "gedijd"},
			{"dingen", "ding", "dingen", "dingt", "dong", "dongen", "gedongen"},
			{"doen", "daden", "dede", "deden", "deed", "doe", "doen", "doet", "dééd", "gedaan"},
			{"dorsen", "dors", "dorsen", "dorst", "dorste", "dorsten", "gedorsen", "gedorst"},
			{"dragen", "draag", "draagde", "draagt", "dragen", "droeg", "droegen", "gedragen"},
			{"driegen", "drieg", "driegen", "driegt", "drogen", "droog", "gedrogen"},
			{"drijven", "dreef", "dreven", "drijf", "drijft", "drijven", "gedreven"},
			{"dringen", "dring", "dringen", "dringt", "drong", "drongen", "gedrongen"},
			{"drinken", "drink", "drinken", "drinkt", "dronk", "dronken", "gedronken"},
			{"drinten", "drinten", "gedronten"},
			{"druipen", "droop", "dropen", "druip", "druipen", "druipt", "druipte", "druipten", "gedropen", "gedruipt"},
			{"duiken", "doken", "dook", "duik", "duiken", "duikt", "duikte", "duikten", "gedoken"},
			{"dunken", "docht", "dochten", "dunken", "dunkt", "gedocht"},
			{"durven", "dierf", "dierven", "dorst", "dorsten", "durven"},
			{"dwaan", "dwaan", "gedwogen"},
			{"dwijnen", "dween", "dwenen", "dwijnen", "gedwenen"},
			{"dwingen", "dwanc", "dwing", "dwingen", "dwingt", "dwong", "dwongen", "gedwongen"},
			{"eten", "at", "aten", "eet", "eten", "gegeten", "éten"},
			{"fletsen", "fletsen", "geflotsen"},
			{"flinken", "flinken", "flonk", "flonken"},
			{"fluiten", "floot", "floten", "fluit", "fluiten", "gefloten"},
			{"gaan", "ga", "gaan", "gaat", "gegaan", "ging", "gingen", "gong", "gongen", "gáán", "gáát"},
			{"gelden", "gegolden", "gegouwen", "geld", "gelden", "geldt", "gold", "golden"},
			{"genezen", "genas", "genazen", "genezen"},
			{"genieten", "genieten", "genoot", "genoten"},
			{"geschieden", "geschach", "geschieden"},
			{"geven", "gaaft", "gaf", "gaven", "gegeven", "geven"},
			{"gieten", "gegoten", "gieten", "goot", "goten"},
			{"glijden", "gegleden", "gleden", "gleed", "glijd", "glijden", "glijdt"},
			{"glimmen", "geglommen", "glim", "glimmen", "glimt", "glom", "glommen"},
			{"glimpen", "geglompen", "glimpen", "glomp", "glompen"},
			{"graven", "gegraven", "graaf", "graaft", "graven", "groef", "groeven"},
			{"grienen", "gegrenen", "gegriend", "green", "grenen", "grien", "griende", "grienden", "grienen", "grient"},
			{"grijpen", "gegrepen", "greep", "grepen", "grijp", "grijpen", "grijpt"},
			{"grijzen", "gegrezen", "grees", "grezen", "grijst", "grijzen"},
			{"groeien", "gegroeid", "groei", "groeide", "groeiden", "groeien", "groeit"},
			{"gunnen", "gegost", "gegund", "gun", "gunde", "gunden", "gunnen", "gunt", "jan"},
			{"halen", "gehaald", "haal", "haalde", "haalden", "haalt", "hale", "halen", "hiel", "hielden", "hielen"},
			{"hangen", "gehangen", "gehongen", "hang", "hangen", "hangt", "hing", "hingen", "hong", "hongen"},
			{"hebben", "gehad", "ha", "haan", "had", "hadde", "hadden", "hae", "haij", "heb", "hebbe", "hebben", "hebt", "heeft", "hád", "hèb", "héb", "hébben", "hééft"},
			{"heten", "geheeten", "gehieten", "heet", "heeten", "heette", "heetten", "hiet", "hieten"},
			{"heffen", "geheven", "gehoffen", "goffen", "hef", "heffen", "heft", "hief", "hieven"},
			{"helen", "geheeld", "heel", "heelde", "heelden", "heelt", "helen"},
			{"helpen", "geholpen", "gehulpen", "halp", "help", "helpen", "helpt", "hielp", "hielpen", "holp", "holpen", "hulp", "hulpen"},
			{"hijsen", "gehesen", "hees", "hesen", "hijs", "hijsen", "hijst"},
			{"hoeven", "gehoeven", "hoeven"},
			{"houden", "gehouden", "helden", "helt", "hiel", "hield", "hielden", "hielen", "hieuw", "hieuwen", "hilden", "hilt", "hou", "houd", "houde", "houden", "houdt"},
			{"houwen", "gehouden", "gehouwen", "hief", "hiel", "hield", "hielden", "hielen", "hieuw", "hieuwen", "houw", "houwen", "houwt"},
			//{"indruisen", "droos", "in", "drozen", "in", "indruisen", "ingedrozen"},
			{"jagen", "gejagen", "jagen", "joeg", "joegen"},
			{"kerven", "gekerfd", "gekorven", "kerf", "kerfde", "kerfden", "kerft", "kerven", "korf", "korven"},
			{"kiezen", "gekoren", "gekozen", "kies", "kiest", "kiezen", "koos", "koren", "kozen"},
			{"kijken", "gekeken", "keek", "keken", "kijk", "kijken", "kijkt", "kéék"},
			{"kijven", "gekeven", "keef", "keven", "kijf", "kijft", "kijven"},
			{"klagen", "geklaagd", "klaag", "klaagde", "klaagden", "klaagt", "klagen", "kloeg", "kloegen"},
			{"klieven", "gekloven", "klieven", "kloof", "kloven"},
			{"klimmen", "geklommen", "klim", "klimmen", "klimt", "klom", "klommen"},
			{"klingen", "klingen", "klongen"},
			{"klinken", "geklonken", "klink", "klinken", "klinkt", "klonk", "klonken"},
			{"klinken", "geklonken", "klink", "klinken", "klinkt", "klonk", "klonken"},
			{"klinken", "geklonken", "klink", "klinken", "klinkt", "klonk", "klonken"},
			{"kluiven", "gekloven", "kloof", "kloven", "kluif", "kluift", "kluiven"},
			{"knarpen", "geknarpt", "knarp", "knarpen", "knarpt", "knarpte", "knarpten", "knierp", "knierpen"},
			{"kniezen", "geknesen", "gekniesd", "knees", "knesen", "knies", "kniesde", "kniesden", "kniest", "kniezen"},
			{"knijpen", "geknepen", "kneep", "knepen", "knijp", "knijpen", "knijpt"},
			{"komen", "gekomen", "kaam", "kaamt", "kamen", "kom", "kome", "komen", "komt", "kwaam", "kwam", "kwamen"},
			{"kopen", "gekocht", "gekoft", "kocht", "kochten", "koft", "kofte", "koften", "koop", "koopen", "koopt"},
			{"krijgen", "gekregen", "kreeg", "kregen", "krijg", "krijgen", "krijgt"},
			{"krijsen", "gekresen", "gekrijst", "krees", "kresen", "krijs", "krijsen", "krijst", "krijste", "krijsten"},
			{"krijten", "gekreten", "kreet", "kreten", "krijt", "krijten"},
			{"krimpen", "gekrompen", "krimp", "krimpen", "krimpt", "kromp", "krompen"},
			{"kringen", "gekrongen", "kringen", "krong"},
			{"kruien", "gekroden", "gekrooien", "gekruid", "kroden", "kroo", "krood", "krooi", "krooien", "krui", "kruide", "kruiden", "kruien", "kruit"},
			{"kruipen", "gekropen", "kroop", "kropen", "kruip", "kruipen", "kruipt"},
			{"kunnen", "gekonnen", "kon", "kond", "konde", "konden", "kost", "kosten", "kunde", "kunnen"},
			{"kwelen", "gekweeld", "kwal", "kweel", "kweelde", "kweelden", "kweelt", "kwelen"},
			{"kwellen", "gekwollen", "kwellen", "kwol"},
			{"kwellen", "kwel", "kwellen", "kwelt"},
			{"kwijnen", "gekwenen", "gekwijnd", "kwijn", "kwijnde", "kwijnden", "kwijnen", "kwijnt"},
			{"kwijten", "gekweten", "kweet", "kweten", "kwijten"},
			{"lachen", "gelachen", "lach", "lachen", "lacht", "lachte", "lachten", "loech", "loechen"},
			{"laden", "geladen", "laad", "laadde", "laadden", "laadt", "laden", "loed", "loeden"},
			{"laden", "geladen", "laad", "laadde", "laadden", "laadt", "laden"},
			{"laten", "gelaten", "laat", "laten", "liet", "lieten", "láát"},
			{"leggen", "geleid", "leggen", "leide", "leiden"},
			{"lezen", "gelezen", "las", "lazen", "lees", "leest", "leze", "lezen"},
			{"liegen", "gelogen", "lieg", "liegen", "liegt", "logen", "loog"},
			{"liggen", "gelegen", "lag", "lagen", "lig", "liggen", "ligt"},
			{"lijden", "geleden", "leden", "leed", "lijd", "lijden", "lijdt"},
			{"lijden", "geleden", "leden", "leed", "lijd", "lijden", "lijdt"},
			{"lijden", "geleden", "leden", "lee", "leed", "lijd", "lijden", "lijdt"},
			{"lijken", "geleken", "leek", "leken", "lijk", "lijken", "lijkt"},
			{"lopen", "geloopen", "liep", "liepen", "loop", "loopen", "loopt"},
			{"luiden", "geluid", "luid", "luidde", "luidden", "luiden", "luidt"},
			{"luiken", "geloken", "loken", "look", "luik", "luiken", "luikt"},
			{"maken", "gemaakt", "maak", "maakt", "maakte", "maakten", "maken", "miek", "mieken"},
			{"malen", "gemalen", "maal", "maalde", "maalden", "maalt", "malen", "moel", "moelen"},
			{"melken", "gemelkt", "gemolken", "melk", "melken", "melkt", "melkte", "melkten", "molk", "molken"},
			{"meten", "gemeten", "mat", "maten", "meet", "meten"},
			{"mijden", "gemeden", "meden", "meed", "mijd", "mijden", "mijdt"},
			{"moeten", "gemoeten", "moest", "moesten", "moet", "moeten", "most", "mosten", "moést", "moét", "moéten"},
			{"mogen", "gemogen", "mocht", "mochten", "mogen"},
			{"nemen", "genomen", "nam", "namen", "neem", "neemt", "neme", "nemen"},
			{"nieten", "geniet", "nieten", "niette", "nietten", "noot", "noten"},
			{"nijgen", "genegen", "neeg", "negen", "nijg", "nijgen", "nijgt"},
			{"nijpen", "genepen", "neep", "nepen", "nijp", "nijpen", "nijpt"},
			{"omzwinden", "omgezwonden", "omzwinden", "zwond", "om"},
			{"ontginnen", "ontginnen", "ontgon", "ontgonnen", "ontgost", "ontgosten"},
			{"ontpluiken", "ontploken", "ontplook", "ontpluiken"},
			{"opgelpen", "opgegolpen", "opgelpen"},
			{"opslorpen", "opgeslorpen", "opslorpen"},
			{"overstelpen", "overstelpen", "overstolp"},
			{"pijpen", "gepepen", "gepijpt", "peep", "pepen", "pijp", "pijpen", "pijpt", "pijpte", "pijpten"},
			{"plegen", "gepleegd", "geplogen", "placht", "plachten", "plag", "plagen", "plecht", "plechten", "pleeg", "pleegde", "pleegden", "pleegt", "plegen", "plocht", "plochten", "plogen", "ploog"},
			{"pleiten", "gepleit", "gepleten", "pleet", "pleit", "pleiten", "pleitte", "pleitten", "pleten"},
			{"plokken", "geplokken", "plokken"},
			{"pluizen", "geplozen", "gepluisd", "ploos", "plozen", "pluis", "pluisde", "pluisden", "pluist", "pluizen"},
			{"praten", "gepraat", "gepraten", "praat", "praatte", "praatten", "praten"},
			{"prijzen", "geprezen", "geprijsd", "prees", "prezen", "prijs", "prijsde", "prijsden", "prijst", "prijzen"},
			{"raden", "geraden", "raad", "raadde", "raadden", "raadt", "raden", "ried", "rieden"},
			{"reken", "rak", "reken"},
			{"rennen", "gerend", "ren", "rende", "renden", "rennen", "rent"},
			{"rijden", "gereden", "reden", "reed", "rij", "rijd", "rijden", "rijdt"},
			{"rijgen", "geregen", "reeg", "regen", "rijg", "rijgen", "rijgt"},
			{"rijpen", "gerepen", "gerijpt", "reep", "repen", "rijp", "rijpen", "rijpt", "rijpte", "rijpten"},
			{"rijten", "gereten", "reet", "reten", "rijt", "rijten"},
			{"rijven", "gereven", "reef", "reven", "rijf", "rijft", "rijven"},
			{"rijzen", "gerezen", "rees", "rezen", "rijs", "rijst", "rijzen"},
			{"ringen", "geringd", "ring", "ringde", "ringden", "ringen", "ringt"},
			{"rinnen", "geronnen", "rinnen"},
			{"roepen", "geroepen", "geropen", "riep", "riepen", "roep", "roepen", "roept"},
			{"rouwen", "gerouwd", "rouw", "rouwde", "rouwden", "rouwen", "rouwt"},
			{"ruiken", "geroken", "roken", "rook", "ruik", "ruiken", "ruikt"},
			{"ruilen", "gerolen", "geruild", "ruil", "ruilde", "ruilden", "ruilen", "ruilt"},
			{"ruiven", "geroven", "geruifd", "ruifde", "ruifden", "ruift", "ruiven"},
			{"ruizen", "gerozen", "roos", "rozen", "ruizen"},
			{"runnen", "geronnen", "gerund", "run", "runde", "runden", "runnen", "runt"},
			{"scheiden", "gescheiden", "scheid", "scheidde", "scheidden", "scheiden", "scheidt"},
			{"schelden", "gescholden", "scheld", "schelden", "scheldt", "schold", "scholden"},
			{"schenden", "geschonden", "schend", "schenden", "schendt", "schond", "schonden"},
			{"schenken", "geschonken", "schenk", "schenken", "schenkt", "schonk", "schonken"},
			{"scheppen", "geschapen", "schep", "scheppen", "schept", "schiep", "schiepen"},
			{"scheppen", "geschapen", "geschept", "schep", "scheppen", "schept", "schepte", "schepten", "schiep", "schiepen"},
			{"scheren", "gescheerd", "geschoren", "scheer", "scheerde", "scheerden", "scheert", "scheren", "schoor", "schoren"},
			{"scheren", "gescheerd", "geschoren", "scheer", "scheerde", "scheerden", "scheert", "scheren", "schoor", "schoren"},
			{"scheren", "gescheerd", "geschoren", "scheer", "scheerde", "scheerden", "scheert", "scheren", "schoor", "schoren"},
			{"schieten", "geschoten", "schiet", "schieten", "schoot", "schoten"},
			{"schijnen", "geschenen", "scheen", "schenen", "schijn", "schijnde", "schijnden", "schijnen", "schijnt"},
			{"schijten", "gescheten", "scheet", "scheten", "schijt", "schijten"},
			{"schillen", "geschild", "geschollen", "schil", "schilde", "schilden", "schillen", "schilt", "schol"},
			{"schrijden", "geschreden", "schreden", "schreed", "schrijden"},
			{"schrijven", "geschreven", "schreef", "schreven", "schrijf", "schrijft", "schrijve", "schrijven"},
			{"schrikken", "geschrokken", "schrik", "schrikken", "schrikt", "schrok", "schrokken"},
			{"schuilen", "gescholen", "geschuild", "scholen", "school", "schuil", "schuilde", "schuilden", "schuilen", "schuilt"},
			{"schuiven", "geschoven", "schoof", "schoven", "schuif", "schuift", "schuiven"},
			{"slaan", "geslagen", "geslegen", "sla", "slaan", "slaat", "sloeg", "sloegen"},
			{"slapen", "geslapen", "slaap", "slaapt", "slapen", "sliep", "sliepen"},
			{"slijpen", "geslepen", "sleep", "slepen", "slijp", "slijpen", "slijpt"},
			{"slijten", "gesleten", "sleet", "sleten", "slijt", "slijten"},
			{"slinden", "slinden", "slond", "slonden"},
			{"slingen", "slingen", "slong", "slongen"},
			{"slinken", "geslonken", "slink", "slinken", "slinkt", "slonk", "slonken"},
			{"sluiken", "gesloken", "sluiken"},
			{"sluipen", "geslopen", "sloop", "slopen", "sluip", "sluipen", "sluipt"},
			{"sluiten", "gesloten", "sloot", "sloten", "sluit", "sluiten"},
			{"smelten", "gesmolten", "smelt", "smelten", "smolt", "smolten"},
			{"smijten", "gesmeten", "smeet", "smeten", "smijt", "smijten"},
			{"snijden", "gesneden", "sneden", "sneed", "snij", "snijd", "snijden", "snijdt"},
			{"snuiten", "gesnoten", "snoot", "snoten", "snuit", "snuiten"},
			{"snuiven", "gesnoven", "snoof", "snoven", "snuif", "snuift", "snuiven"},
			{"spannen", "gespannen", "spannen"},
			{"spijten", "gespeten", "speet", "speten", "spijt", "spijten"},
			{"spinnen", "gesponnen", "spin", "spinnen", "spint", "spon", "sponnen"},
			{"splijten", "gespleten", "spleet", "spleten", "splijten"},
			{"spouwen", "gespouwen", "spouwen"},
			{"spreken", "gesproken", "sprak", "spraken", "spreek", "spreekt", "spreken"},
			{"springen", "gesprongen", "spring", "springen", "springt", "sprong", "sprongen"},
			{"spruiten", "gesproten", "sproot", "sproten", "spruit", "spruiten"},
			{"spugen", "gespogen", "gespuugd", "spogen", "spoog", "spugen", "spuug", "spuugde", "spuugden", "spuugt"},
			{"spuiten", "gespoten", "spoot", "spoten", "spuit", "spuiten"},
			{"staan", "gestaan", "sta", "staan", "staat", "stond", "stonden", "stáát"},
			{"standen", "standen"},
			{"stappen", "gestapt", "stap", "stappen", "stapt", "stapte", "stapten"},
			{"steken", "gestoken", "stak", "staken", "steken"},
			{"stelen", "gestolen", "stal", "stalen", "steel", "steelt", "stelen", "stolen", "stool"},
			{"stelpen", "gestolpen", "stelpen"},
			{"stenen", "gesteend", "stan", "steende", "steenden", "steent"},
			{"sterven", "gestorven", "starf", "starven", "sterf", "sterft", "sterven", "stierf", "stierven"},
			{"stiepen", "stiepen"},
			{"stijgen", "gestegen", "steeg", "stegen", "stijg", "stijgen", "stijgt"},
			{"stijven", "gesteven", "gestijfd", "steef", "steven", "stijf", "stijfde", "stijfden", "stijft", "stijven"},
			{"stinken", "gestonken", "stink", "stinken", "stinkt", "stonk", "stonken"},
			{"stoten", "gestooten", "stiet", "stieten", "stoot", "stooten", "stootte", "stootten"},
			{"strijden", "gestreden", "streden", "streed", "strijd", "strijden", "strijdt"},
			{"strijken", "gestreken", "gestroken", "streek", "streken", "strijk", "strijken", "strijkt"},
			{"strijven", "streef", "streven", "strijven"},
			{"strikken", "gestrikt", "gestrokken", "strik", "strikken", "strikt", "strikte", "strikten"},
			{"stuiken", "gestoken", "gestuikt", "stoken", "stook", "stuik", "stuiken", "stuikt", "stuikte", "stuikten"},
			{"stuipen", "gestopen", "stoop", "stopen", "stuipen"},
			{"stuiven", "gestoven", "stoof", "stoven", "stuif", "stuift", "stuiven"},
			{"tijgen", "getagen", "getegen", "getogen", "teeg", "tegen", "tijg", "tijgen", "tijgt", "togen", "toog"},
			{"tijgen", "getogen", "tijg", "tijgen", "tijgt", "togen", "toog"},
			{"tinken", "getinkt", "getonken", "tink", "tinken", "tinkt", "tinkte", "tinkten"},
			{"treden", "getreden", "trad", "traden", "treden", "treed", "treedt"},
			{"treffen", "getreffen", "getroffen", "tref", "treffen", "treft", "trof", "troffen", "troft", "troften"},
			{"trekken", "getrokken", "trak", "trek", "trekken", "trekt", "triek", "trok", "trokken", "trokte"},
			{"tuiten", "getuit", "tuit", "tuiten", "tuitte", "tuitten"},
			{"uitscheiden", "scheden", "uit", "schee", "uit", "scheed", "uit", "scheeën", "uit", "uitgescheeën",  "uitgescheiden", "uitscheiden"},
			{"uittreden", "tard", "uit", "tarden", "uit", "tord", "uit", "torden", "uit", "trad", "uit", "traden", "uit", "uitgetorden", "uitgetorten", "uitgetreden", "uitgetroden", "uittreden"},
			{"vaan", "gevaan", "vaan"},
			{"vallen", "gevallen", "vallen", "viel", "vielen"},
			{"vangen", "gevangen", "gevongen", "vang", "vangde", "vangen", "vangt", "ving", "vingen", "vong", "vongen"},
			{"varen", "gevaard", "gevaren", "vaar", "vaarde", "vaarden", "vaart", "varen", "voer", "voeren"},
			{"vechten", "gevochten", "vecht", "vechten", "vechtte", "vocht", "vochten", "vucht"},
			{"verbaren", "verbaren", "verboer", "verboeren"},
			{"verdrieten", "verdrieten", "verdroot", "verdroten"},
			{"vergeten", "vergat", "vergaten", "vergeten"},
			{"verhelen", "verhelen", "verholen"},
			{"verkleumen", "verkleumen", "verklommen", "verklonnen"},
			{"verkouden", "verkouden", "verkouwerd"},
			{"verliezen", "verliezen", "verloor", "verloos", "verloren", "verlozen"},
			{"verschrimpen", "verschrimpen", "verschromp", "verschrompen", "verschrompt"},
			{"verslinden", "verslinden", "verslond", "verslonden"},
			{"verstaan", "verstaan", "verstand", "verstanden", "verstoed", "verstond", "verstonden"},
			{"verwaten", "verwaten", "verwatend"},
			{"verwennen", "verwennen", "verwon", "verwonnen"},
			{"verzijpen", "verzepen", "verzijpen", "verzopen"},
			{"verzwinden", "verzwinden", "verzwond", "verzwonden"},
			{"vijlen", "gevijld", "veel", "velen", "vijl", "vijlde", "vijlden", "vijlen", "vijlt"},
			{"vijsten", "geveesten", "veest", "veesten", "vijsten"},
			{"vijzen", "gevezen", "vees", "vezen", "vijs", "vijst", "vijzen"},
			{"vinden", "gevonden", "gevongen", "van", "vand", "vanden", "vind", "vinden", "vindt", "vingden", "von", "vond", "vonden", "vong", "vongen"},
			{"vlaan", "gevladen", "gevlegen", "vlaan", "vleeg", "vlegen"},
			{"vlechten", "gevlocht", "gevlochten", "gevluchten", "vlecht", "vlechten", "vlocht", "vlochten"},
			{"vleien", "gevleid", "vlei", "vleide", "vleiden", "vleien", "vleit"},
			{"vlieden", "gevloden", "vlied", "vlieden", "vliedt", "vloden", "vlood"},
			{"vliegen", "gevlogen", "vlieg", "vliegen", "vliegt", "vlogen", "vloog"},
			{"vlieten", "gevloten", "vliet", "vlieten", "vloot", "vloten"},
			{"vlijen", "gevleden", "gevleeën", "gevlegen", "gevleten", "gevlijd", "vleden", "vleed", "vlij", "vlijde", "vlijden", "vlijen", "vlijt"},
			{"vouwen", "gevold", "gevouwd", "gevouwen", "voldede", "voldeden", "vouw", "vouwde", "vouwden", "vouwen", "vouwt"},
			{"vragen", "gevragen", "vragen", "vrieg", "vriegen", "vroeg", "vroegen"},
			{"vreten", "gevreten", "vrat", "vraten", "vreet", "vreten"},
			{"vriezen", "gevroren", "gevrozen", "vries", "vriest", "vriezen", "vroor", "vroos", "vroren", "vrozen"},
			{"vrijen", "gevreeen", "gevreeën", "gevrejen", "gevrijd", "vree", "vreeen", "vreeën", "vrij", "vrijde", "vrijden", "vrijen", "vrijt"},
			{"waaien", "gewaaid", "waai", "waaide", "waaiden", "waaien", "waait", "woei", "woeien"},
			{"wassen", "gewassen", "gewossen", "was", "wassen", "wast", "waste", "wasten", "wies", "wiessen", "wos", "wossen"},
			{"wegbelenden", "wegbelenden", "weggeblonden", "weggeblonnen"},
			{"wegen", "gewagen", "gewegen", "gewoegen", "gewogen", "weeg", "weegt", "wegen", "woeg", "woegen", "wogen", "woog"},
			{"welven", "gewelfd", "welf", "welfde", "welfden", "welft", "welven"},
			{"wensen", "gewenst", "wens", "wensen", "wenst", "wenste", "wensten", "wons", "wonsen"},
			{"werken", "gewerkt", "gewrocht", "werk", "werken", "werkt", "werkte", "werkten", "wracht", "wrachten", "wrocht", "wrochten"},
			{"werpen", "geworpen", "warp", "warpen", "werp", "werpen", "werpt", "wierp", "wierpen", "worp", "worpen", "wurp", "wurpen"},
			{"werven", "geworven", "gewurven", "werf", "werft", "werven", "wierf", "wierven", "worf", "worven", "wurf", "wurven"},
			{"weten", "geweten", "weet", "weette", "weetten", "wetede", "weteden", "weten", "wetende", "wetenden", "wist", "wisten", "wost", "wosten", "wust", "wusten", "wéten", "wéét"},
			{"weven", "geweefd", "geweven", "gewoven", "weef", "weefde", "weefden", "weeft", "weven", "woef", "woeven"},
			{"wezen", "wezen"},
			{"wijden", "gewijd", "wijd", "wijdde", "wijdden", "wijden", "wijdt"},
			{"wijken", "geweken", "week", "weken", "wijk", "wijken", "wijkt"},
			{"wijten", "geweten", "weet", "weten", "wijt", "wijten", "wéten", "wéét"},
			{"wijzen", "gewezen", "wees", "wezen", "wijs", "wijst", "wijzen"},
			{"willen", "gewild", "gewold", "gewód", "wa", "waje", "wil", "wilde", "wilden", "willen", "wilt", "wod", "wol", "wolde", "wolden", "wollen", "wou", "woude", "wouden"},
			{"winden", "gewonden", "wind", "winden", "windt", "wond", "wonden"},
			{"winken", "winken", "wonk", "wonken"},
			{"winnen", "gewonnen", "wan", "want", "win", "winnen", "wint", "won", "wonnen", "wont"},
			{"worden", "gewoorden", "geworden", "werd", "werden", "widde", "wier", "wierd", "wierden", "wieren", "wit", "wodde", "woer", "wood", "woord", "woorden", "word", "worde", "worden", "wordt", "worren", "wuir", "wòrde", "wòrre"},
			{"wreken", "gewreekt", "gewroken", "wrak", "wreek", "wreekt", "wreekte", "wreekten", "wreken", "wroke", "wroken", "wrook", "wrookt"},
			{"wrijven", "gewreven", "wreef", "wreven", "wrijf", "wrijfde", "wrijft", "wrijven"},
			{"wringen", "gewrongen", "wring", "wringen", "wringt", "wrong", "wrongen"},
			{"wuiven", "gewoven", "gewuifd", "woof", "woven", "wuif", "wuifde", "wuifden", "wuift", "wuiven"},
			{"zeggen", "gezaag", "gezeed", "gezegd", "gezeid", "sagte", "sagten", "zee", "zeeën", "zeg", "zegde", "zegden", "zegge", "zeggen", "zegt", "zei", "zeide", "zeiden", "zeien", "zég", "zégt"},
			{"zeiken", "gezeikt", "gezeken", "zeek", "zeik", "zeiken", "zeikt", "zeikte", "zeikten", "zeken"},
			{"zenden", "gezonden", "zand", "zend", "zenden", "zendt", "zond", "zonden"},
			{"zieden", "gezoden", "zied", "ziedde", "ziedden", "zieden", "ziedt", "zoden", "zood"},
			{"zien", "gezien", "zag", "zagen", "zie", "zien", "ziet"},
			{"zijgen", "gezegen", "zeeg", "zegen", "zijg", "zijgen", "zijgt"},
			{"zijn", "ben", "bent", "bèn", "bén", "geweest", "gewezen", "is", "waar", "waart", "wadden", "ware", "waren", "was", "wasse", "wassen", "wazen", "wazzen", "wees", "wier", "wáren", "wás", "zijn", "ìs"},
			{"zingen", "gezongen", "zang", "zank", "zing", "zingden", "zingen", "zingt", "zong", "zongen"},
			{"zinken", "gezonken", "zank", "zink", "zinken", "zinkt", "zonk", "zonken"},
			{"zinnen", "gezonnen", "zin", "zinde", "zinnen", "zint", "zon", "zonnen"},
			{"zitten", "gezeten", "zat", "zaten", "zit", "zitten"},
			{"zoeken", "gezocht", "gezoken", "zocht", "zochten", "zoek", "zoeken", "zoekt"},
			{"zouten", "gezouten", "zout", "zouten", "zoutte", "zoutten"},
			{"zuigen", "gezogen", "zogen", "zoog", "zuig", "zuigen", "zuigt"},
			{"zuiken", "gezoken", "zuiken"},
			{"zuipen", "gezopen", "zoop", "zopen", "zuip", "zuipen", "zuipt"},
			{"zullen", "zoe", "zoeden", "zoen", "zol", "zolde", "zolden", "zolen", "zollen", "zoo", "zooen", "zou", "zoude", "zouden", "zuld??", "zullen"},
			{"zwelgen", "gezwolgen", "zwalg", "zwelg", "zwelgen", "zwelgt", "zwolg", "zwolgen"},
			{"zwellen", "gezwollen", "zwel", "zwellen", "zwelt", "zwol", "zwollen"},
			{"zwelten", "gezwolten", "zwelten", "zwolt", "zwolten"},
			{"zwemmen", "gezwommen", "zwam", "zwem", "zwemmen", "zwemt", "zwom", "zwommen"},
			{"zwenken", "gezwenkt", "gezwonken", "zwank", "zwenk", "zwenken", "zwenkt", "zwenkte", "zwenkten", "zwonken"},
			{"zweren", "gezworen", "zoer", "zweer", "zweerde", "zweerden", "zweert", "zweren", "zwoer", "zwoeren", "zwoor", "zworen"},
			{"zweren", "gezworen", "zweer", "zweerde", "zweerden", "zweert", "zweren", "zwoer", "zwoeren", "zwoor", "zworen"},
			{"zwerven", "gezworven", "zwerf", "zwerft", "zwerven", "zwierf", "zwierven", "zworf", "zworven"},
			{"zweven", "gezweefd", "zweef", "zweefde", "zweefden", "zweeft", "zweven"},
			{"zwijgen", "gezwegen", "zweeg", "zwegen", "zwijg", "zwijgen", "zwijgt"},
			{"zwijken", "gezweken", "zweken", "zwijken"},
			{"zwijmen", "gezwijmd", "zweem", "zwemen", "zwijm", "zwijmde", "zwijmden", "zwijmen", "zwijmt"},
			{"zwijnen", "gezwijnd", "zwijn", "zwijnde", "zwijnden", "zwijnen", "zwijnt"},
			{"zwijpen", "zweep", "zwepen", "zwijpen"},
			{"zwinden", "zwinden", "zwond"},
			{"zwingen", "zwang", "zwingen", "zwong"},
			{"zwinken", "gezwonken", "zwinken"},
			//{"afvleten", "afgevleten", "afvleten"},
		};
}
