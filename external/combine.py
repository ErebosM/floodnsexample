import os

for f in [1, 200, 400, 600, 800, 1000, 1500, 2000, 2500, 3000, 3500]:
    # Retrieve statistic we want
    filename = "../temp/demo_" + str(f) + "/analysis/connection_info_lb_0_ub_10000000000.statistics"
    if os.path.isfile(filename):
        with open(filename, "r") as f_in:
            for j in f_in:
                if j.strip().split("=")[0].strip() == "completed_connection_fct_mean":
                    print(str(f) + " " + j.strip().split("=")[1].strip())
