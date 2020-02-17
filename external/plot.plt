###
###  Released under the MIT License (MIT) --- see ../LICENSE
###  Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong,
###  Lucian Popa, P. Brighten Godfrey, Alexandra Kolla, Simon Kassing
###

#####################################
### STYLING

# Terminal (gnuplot 4.4+); Swiss neutral Helvetica font
set terminal pdfcairo font "Helvetica, 24" linewidth 1.5 rounded dashed

# Line style for axes
set style line 80 lt rgb "#808080"

# Line style for grid
set style line 81 lt 0  # Dashed
set style line 81 lt rgb "#999999"  # Grey grid

# Grey grid and border
set grid back linestyle 81
set border 3 back linestyle 80
set xtics nomirror
set ytics nomirror

# Line styles
set style line 1 lt rgb "#2177b0" lw 2.4 pt 1 ps 1.4
set style line 2 lt rgb "#fc7f2b" lw 2.4 pt 0 ps 1.4
set style line 3 lt rgb "#2f9e37" lw 2.4 pt 0 ps 1.4
set style line 4 lt rgb "#d42a2d" lw 2.4 pt 0 ps 1.4
set style line 5 lt rgb "#80007F" lw 2.4 pt 0 ps 1.4
set style line 6 lt rgb "#8a554c" lw 2.4 pt 0 ps 0 dt 3
set style line 7 lt rgb "#e079be" lw 2.4 pt 0 ps 1.4
set style line 8 lt rgb "#7d7d7d" lw 2.4 pt 0 ps 1.4
set style line 9 lt rgb "#000000" lw 2.4 pt 0 ps 1.4

# Output
set output "plot.pdf"

#####################################
### AXES AND KEY

# Axes labels
set xlabel "Flow arrival rate (flows/s)" # Markup: e.g. 99^{th}, {/Symbol s}, {/Helvetica-Italic P}
set ylabel "Mean FCT (ms)"

# Axes ranges
set xrange [0:3000]       # Explicitly set the x-range [lower:upper]
set yrange [0:400]       # Explicitly set the y-range [lower:upper]
# set xtics <start>, <incr> {,<end>}
# set ytics <start>, <incr> {,<end>}
# set format x "%.2f%%"  # Set the x-tic format, e.g. in this case it takes 2 sign. decimals: "24.13%""
# set format y "%.0fK"   # Set the y-tic format, e.g. in this case it rounds to decimal: "3352K"

# For logarithmic axes
# set log x           # Set logarithmic x-axis
# set log y           # Set logarithmic y-axis
# set mxtics 3        # Set number of intermediate tics on x-axis (for log plots)
# set mytics 3        # Set number of intermediate tics on y-axis (for log plots)

# Font of the key (a.k.a. legend)
set key font ",16"          # Adapt to lower if it must fit a busy plot
set key reverse
set key bottom right Left
#set key tmargin            # Force the key to be outside of the plot
set bmargin 3.3
set key spacing 1.6

#####################################
### PLOTS

plot    "test.txt" using 1:($2 / 1000000) title "" w lp ls 1, \
