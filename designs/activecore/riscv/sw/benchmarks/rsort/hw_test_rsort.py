# -*- coding:utf-8 -*-
from __future__ import division

import sys
sys.path.append('../../../../../rtl/udm/sw')

import time

import udm
from udm import *

def hw_test_rsort(rsort_filename):
    print("#### RSORT TEST STARTED ####");
    
    DATA_SIZE = 1024
    verify_data = [
        900852, 2196420, 2530146, 3159407, 3522194, 3794415, 5309142, 9736243, 12937811, 13887419, 14125900, 16411608, 19227407, 20643769, 22136821, 22762734, 25885333, 26687179, 27377406, 28256901, 
      30992839, 31153367, 31574181, 31596852, 35859252, 37141933, 41714278, 42021322, 47036070, 47452797, 47463938, 58217952, 62209567, 64795664, 66579049, 69118708, 70003859, 70894786, 72802865, 72939206, 
      77482422, 78348530, 80321564, 81402251, 84924801, 87897457, 88823122, 89400484, 90063199, 92620223, 93191015, 95752736, 106223060, 106595511, 107095939, 107160610, 109035776, 109526756, 114202152, 116766659, 
      117260224, 117766047, 119285206, 123889599, 128036841, 130001148, 131405125, 133790520, 134453363, 135012885, 135678410, 137796456, 140536987, 146002907, 148113917, 152273285, 168771888, 170281493, 173822937, 174082258, 
      174965839, 175753599, 176024101, 178901145, 180599971, 181567810, 183679578, 185592115, 185979744, 186112992, 186178768, 188016786, 190215907, 190536346, 190807078, 193873940, 196401930, 198186681, 204514903, 205008108, 
      206653795, 208160226, 208433088, 210908782, 212821393, 215018112, 221330186, 222529745, 225091211, 225934851, 226723092, 226726100, 226906236, 226986415, 227870124, 229905664, 230340298, 231181585, 237020443, 238278880, 
      238566180, 239990424, 240618723, 244709448, 244975305, 245334962, 250731366, 255226842, 256210446, 256860662, 259768753, 259775677, 260267972, 260670135, 260792255, 261556590, 267174620, 269167950, 270410557, 271906847, 
      274746405, 277497333, 279252402, 283430575, 284658041, 287497702, 293109484, 298799048, 301350822, 301663421, 301673313, 303845878, 304131252, 304211060, 304427548, 305750851, 308700094, 308832070, 316479300, 318750634, 
      320137025, 324215873, 325667019, 329049266, 329767814, 330474090, 333820982, 333906659, 336213879, 338532526, 340231765, 340442582, 341674967, 343119399, 344860208, 346598567, 348131466, 355614494, 356638683, 362748128, 
      365275089, 365854486, 366700722, 367923081, 368541415, 368889391, 372280769, 372465453, 373226849, 373305330, 381571915, 382422699, 387061281, 387611445, 388855203, 389233190, 390743779, 395360213, 398244682, 398911194, 
      399114073, 399456957, 399991238, 402845420, 416457166, 419109342, 419476096, 420106224, 421198539, 422047044, 432609664, 434850545, 435236973, 435288306, 436580096, 436630343, 438711458, 448284065, 452888789, 454359988, 
      460893030, 462743614, 462857778, 464171383, 464795711, 465119445, 470090505, 472872107, 477407584, 478721193, 481561285, 482917205, 486165509, 487888229, 490920543, 492507266, 494367164, 500734709, 502323961, 503368137, 
      507848158, 507936866, 508041515, 509116230, 509744705, 513009937, 514914429, 515811664, 517767440, 518073650, 519344323, 519699747, 522160389, 532467027, 533101409, 535828217, 536063430, 538932155, 539429964, 539942311, 
      540006359, 543952312, 545066288, 546340809, 548632788, 549253917, 556328195, 565150174, 567779677, 568652142, 569949159, 571635069, 572240371, 574236644, 578787063, 579174666, 580692625, 583112200, 585169793, 585804640, 
      589049769, 590690783, 592006699, 592234826, 594814862, 598016845, 602368560, 602907378, 603159718, 603907009, 612289854, 614612685, 617995659, 618681206, 619428858, 619461174, 619972089, 621921213, 622114437, 622170346, 
      624939139, 625991894, 628445699, 631022494, 632611704, 633075975, 637659983, 638167485, 644293952, 649754857, 650513248, 651536866, 651750790, 653050943, 655445946, 656954306, 661318748, 670621648, 671848647, 672906535, 
      675549432, 680172541, 683536723, 686797873, 687941098, 688338919, 688483428, 692845661, 696448050, 702380578, 705851791, 706913763, 714042223, 721046824, 721524505, 722260205, 723323690, 726141970, 726869128, 727587915, 
      729474445, 730422541, 730845665, 732444124, 737518341, 740536012, 741078359, 745183293, 745777837, 747592875, 748527394, 749415493, 750919339, 753296935, 754215794, 754247044, 754896618, 755798022, 764935753, 765386206, 
      767233910, 767880329, 768185796, 769005536, 770412991, 770957259, 772059719, 777433215, 779093898, 783427298, 785398708, 786566648, 787913688, 790929190, 791188057, 793505636, 795915364, 798445606, 799717076, 802730854, 
      806384435, 806638567, 809604334, 810387144, 812328969, 813511681, 814302898, 817417567, 820933470, 821481684, 829450571, 832081018, 833019845, 834410115, 835063788, 837554186, 840106059, 843738737, 846127236, 848402819, 
      849489374, 852388468, 852835634, 855107605, 856837002, 858045289, 858150908, 859612092, 863551327, 863647665, 866478506, 866854601, 866964848, 867628573, 869853078, 871908043, 873623623, 875481479, 876130333, 879552408, 
      879878312, 880456526, 886978116, 890461390, 891710357, 892939912, 893302687, 894769708, 895080280, 895562064, 898893218, 901789102, 909415369, 910677024, 914906004, 915948205, 916073297, 918378387, 919736727, 921573402, 
      923768127, 924541465, 925975225, 927524181, 929351822, 930367387, 932423857, 933603111, 933673987, 935490932, 935689637, 935939763, 937976428, 937987129, 939497524, 942977997, 944382193, 946311527, 946489895, 948686793, 
      949010757, 949222447, 951663731, 953383750, 953877041, 954441852, 959354209, 969076419, 969305448, 973493986, 973769771, 976015092, 977907387, 978247260, 983043723, 985305323, 986240059, 987761406, 991159735, 992410365, 
      993525189, 994291574, 998931662, 1011417836, 1020037994, 1025117441, 1026342885, 1027758817, 1027760887, 1028937430, 1029176122, 1029832956, 1031625002, 1032748996, 1033910502, 1038018856, 1038830638, 1045627895, 1045658065, 1048943084, 
      1051184301, 1058937717, 1061864942, 1063743848, 1065481253, 1067775613, 1068587688, 1068798385, 1073695485, 1073871430, 1074205225, 1075762632, 1078197595, 1079859867, 1082499152, 1087251698, 1092881031, 1094283114, 1094531762, 1095403694, 
      1099847920, 1100041120, 1101933540, 1102423908, 1102603775, 1106678970, 1111213862, 1112731797, 1113693824, 1120963974, 1121432739, 1121800211, 1121960111, 1123331233, 1127173529, 1130850177, 1131661227, 1132597050, 1134185180, 1135793759, 
      1136264020, 1136356724, 1139159604, 1140008063, 1140728569, 1143511498, 1143853106, 1147341731, 1148699143, 1152416171, 1154842325, 1155459206, 1155544307, 1157666831, 1160036198, 1160333307, 1161580432, 1162679490, 1166227930, 1167207852, 
      1169327477, 1170510314, 1170759710, 1175885101, 1179525294, 1181191604, 1184561748, 1185825535, 1188748563, 1191079043, 1191513656, 1192155877, 1194709162, 1198213061, 1198254803, 1199317660, 1201124879, 1202009920, 1202162389, 1203897636, 
      1205895814, 1206302514, 1209784819, 1212628403, 1212974417, 1214379246, 1214950832, 1218522465, 1220915309, 1221108420, 1221726287, 1223475486, 1223572435, 1223583276, 1231149357, 1231249235, 1232992084, 1234944834, 1239665569, 1245819051, 
      1249183285, 1255524953, 1256015513, 1256152080, 1256372950, 1257507406, 1262399341, 1267173039, 1273223611, 1273955724, 1274190723, 1274436639, 1275331596, 1281443423, 1286989824, 1287171290, 1289308008, 1290969161, 1291025252, 1293091635, 
      1296857499, 1302272656, 1302724177, 1303736105, 1312471120, 1313871880, 1316120929, 1319798607, 1321897861, 1322281086, 1323798820, 1331383316, 1331593955, 1332467446, 1334857284, 1335298265, 1335652095, 1335861008, 1336108161, 1337406065, 
      1340949553, 1344125357, 1347284277, 1350653461, 1353144470, 1353999206, 1356847855, 1361012693, 1361078114, 1365423458, 1367731096, 1368235387, 1374125186, 1379259015, 1379460673, 1379995885, 1382429941, 1382484003, 1382551511, 1383640563, 
      1386258142, 1388759892, 1390040163, 1390274260, 1390306889, 1390656632, 1391095995, 1392162662, 1394929399, 1395155215, 1395289708, 1396662140, 1399673078, 1400929335, 1401058771, 1404727477, 1412540552, 1413860940, 1414141069, 1415390230, 
      1417415680, 1417601340, 1420810050, 1424797596, 1431492783, 1431814151, 1434046375, 1435859758, 1440145706, 1441811554, 1444076976, 1450892344, 1451442832, 1453397990, 1455756978, 1459100749, 1459138745, 1459500136, 1462921857, 1466632447, 
      1467376771, 1468486929, 1471131758, 1471598258, 1480273562, 1480437960, 1485740112, 1486594282, 1491477359, 1492071476, 1492488573, 1496078411, 1496277718, 1497680539, 1500102591, 1501181424, 1502480836, 1502557594, 1505022272, 1505475223, 
      1507433029, 1508705426, 1510133268, 1512163910, 1513255537, 1514278798, 1515262458, 1519017268, 1520667645, 1524491858, 1525451290, 1528031307, 1536213818, 1537459540, 1538069400, 1539560507, 1545555937, 1546104436, 1548804416, 1550159640, 
      1551661799, 1553547892, 1558695709, 1561251584, 1561730727, 1563717683, 1565700239, 1566878909, 1568176414, 1568395337, 1568900638, 1569479928, 1571598133, 1572212863, 1575380740, 1577034674, 1581062145, 1588715179, 1590461855, 1590771096, 
      1593359038, 1596821127, 1597506096, 1598436917, 1599876594, 1601247294, 1601617797, 1602517354, 1602967659, 1605369007, 1607204293, 1607515830, 1609955669, 1610244183, 1610539590, 1611500622, 1611680320, 1612593993, 1614780688, 1617229086, 
      1618378831, 1619598456, 1620562432, 1620803320, 1627355806, 1628389501, 1629633514, 1629914848, 1630293700, 1631405417, 1631935240, 1634445325, 1636203720, 1640952769, 1645969422, 1648575360, 1649392096, 1652108025, 1655277291, 1657001479, 
      1663890071, 1667102296, 1672192305, 1674045273, 1675300017, 1679324299, 1680251429, 1681866717, 1685350721, 1686215900, 1686479103, 1689946986, 1690167792, 1691486500, 1695191950, 1695617313, 1702425249, 1704561346, 1706528514, 1708002892, 
      1708126014, 1709823304, 1710305778, 1711718534, 1711761015, 1711765426, 1712269139, 1714295540, 1716853034, 1719009954, 1719573683, 1721767511, 1723143470, 1726987516, 1727583308, 1731949250, 1732028080, 1733366374, 1737931879, 1738353358, 
      1740778973, 1745543275, 1746476698, 1749972876, 1750197960, 1750443194, 1752088215, 1754447491, 1757028186, 1758748737, 1760851607, 1761289401, 1763785295, 1764436465, 1765354961, 1767380006, 1770497338, 1771614266, 1771924274, 1772760484, 
      1774652096, 1774796872, 1775608361, 1775805177, 1776471922, 1776794410, 1778544166, 1781233744, 1790557628, 1792756324, 1793190956, 1800186189, 1800522575, 1802197319, 1807085904, 1807172811, 1811088032, 1816641611, 1817156134, 1821559709, 
      1822612008, 1826628136, 1826880531, 1833603743, 1834161399, 1834285819, 1834318089, 1834388383, 1838218199, 1840896199, 1846605728, 1849495275, 1849675857, 1858933377, 1864952910, 1868289345, 1870214195, 1870859970, 1871684562, 1873900475, 
      1876255297, 1878253960, 1878434988, 1879635915, 1881253854, 1891630185, 1892370478, 1896952705, 1898582764, 1900162831, 1904770864, 1906345724, 1907793490, 1908093581, 1909681194, 1913581092, 1915435344, 1916602098, 1920182565, 1924603748, 
      1928309762, 1928550562, 1930662128, 1931282005, 1933923245, 1938258683, 1939683211, 1942089394, 1943137808, 1944679691, 1946004194, 1948121717, 1949896838, 1952364329, 1958396887, 1962689485, 1965358941, 1967705762, 1968608155, 1970797151, 
      1971484382, 1977865396, 1980923963, 1981481446, 1983379601, 1989240516, 1994402695, 2001056977, 2002881120, 2005817027, 2006320950, 2006856683, 2010549018, 2011827638, 2014090929, 2014129292, 2016161810, 2017191527, 2022291557, 2023498746, 
      2024853642, 2027814180, 2027828650, 2028116487, 2029909894, 2033003588, 2033828953, 2034767768, 2036750157, 2037182006, 2041942843, 2045407567, 2045821218, 2046759770, 2047006719, 2052321529, 2052328978, 2053899162, 2060969286, 2063408339, 
      2064145552, 2066865713, 2070467093, 2072440819, 2074840378, 2074896270, 2076612603, 2082760612, 2084953915, 2085323316, 2088576982, 2090543533, 2091626028, 2094085507, 2097057488, 2100497812, 2105321510, 2105638337, 2105814934, 2109530615, 
      2113434968, 2115445751, 2116495484, 2118203528, 2118821565, 2119425672, 2122262571, 2122852959, 2124749673, 2125958841, 2126029304, 2126940990, 2129513521, 2131761201, 2133066804, 2139976479, 2140091269, 2140756121, 2141591317, 2142357746, 
      2143324534, 2143343312, 2143968639, 2145930822
	]
    
    print("Loading test program...")
    udm.loadbin(rsort_filename)
    print("Test program written!")

    print("Reading data buffer...")
    rdarr = udm.rdarr32(0x6000, DATA_SIZE)
    print("Data buffer read!")

    test_succ_flag = 1
    for i in range(DATA_SIZE):
        if (verify_data[i] != rdarr[i]):
            test_succ_flag = 0
            print("Test failed on data ", i, "! Expected: ", hex(verify_data[i]), ", received: ", hex(rdarr[i]))
    
    if (test_succ_flag):
        print("#### RSORT TEST PASSED! ####");
    else:
        print("#### RSORT TEST FAILED! ####")
    
    print("")    
    return test_succ_flag
