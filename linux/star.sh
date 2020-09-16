#!/bin/bash

tfile=$(echo $0|cut -d'/' -f2)
grep '^###@' $tfile | while read line
do
lin=$(echo $line|cut -d' ' -f2)
wget -c $lin
filename=$(echo $lin|cut -d'/' -f6)
sudo tar jxvf $filename -C /usr/share/stardict/dic/
rm -fr $filename
done

#### 下載連結設定 ###########
# cdict-big5 dictionary(en - zh_TW)
###@ http://nchc.dl.sourceforge.net/sourceforge/stardict/stardict-cdict-big5-2.4.2.tar.bz2
# xdict-ec-big5 dictionary(en - zh_TW)
###@ http://nchc.dl.sourceforge.net/sourceforge/stardict/stardict-xdict-ec-big5-2.4.2.tar.bz2
# xdict-ce-big5 dictionary(zh_TW - en)
###@ http://nchc.dl.sourceforge.net/sourceforge/stardict/stardict-xdict-ce-big5-2.4.2.tar.bz2
# cedict-big5 dictionary(zh_TW - en)
###@ http://nchc.dl.sourceforge.net/sourceforge/stardict/stardict-cedict-big5-2.4.2.tar.bz2
# langdao-ec-big5 dictionary(en - zh_TW) 朗道英漢字典
###@ http://nchc.dl.sourceforge.net/sourceforge/stardict/stardict-langdao-ec-big5-2.4.2.tar.bz2
# langdao-ce-big5 dictionary(zh_TW - en) 朗道漢英字典
###@ http://nchc.dl.sourceforge.net/sourceforge/stardict/stardict-langdao-ce-big5-2.4.2.tar.bz2
# 21 century bidirectional dictionary 21世紀英漢漢英雙向詞典
###@ http://nchc.dl.sourceforge.net/sourceforge/stardict/stardict-21shijishuangxiangcidian-big5-2.4.2.tar.bz2
