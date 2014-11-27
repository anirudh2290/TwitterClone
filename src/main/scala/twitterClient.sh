numUsers=(5000 7000 10000 30000 50000 80000 100000 120000 150000)
numberOfLBs=(2)
isServer=false
#for variable lbs
#echo "\"runMain project4 ${numUsers[$i]} true ${numberOfLBs[$j]}\""
i=0	
while [ $i -lt ${#numUsers[@]} ]
do
	j=0
	while [ $j -lt ${#numberOfLBs[@]} ]
	do
		# numUsers isserver numberofLBs
		#echo ${numUsers[$i]} ${isServer} ${numberOfLBs[$j]}
		sbt "\"runMain project4 ${numUsers[$i]} false ${numberOfLBs[$j]}\"" >> clientfile${numUsers[$i]}
		j=$((j+1))
	done
	i=$((i+1))
done
