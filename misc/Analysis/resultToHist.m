## Copyright (C) 2017 David
## 
## This program is free software; you can redistribute it and/or modify it
## under the terms of the GNU General Public License as published by
## the Free Software Foundation; either version 3 of the License, or
## (at your option) any later version.
## 
## This program is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
## GNU General Public License for more details.
## 
## You should have received a copy of the GNU General Public License
## along with this program.  If not, see <http://www.gnu.org/licenses/>.

## -*- texinfo -*- 
## @deftypefn {Function File} {@var{retval} =} resultToHist (@var{input1}, @var{input2})
##
## @seealso{}
## @end deftypefn

## Author: David <david@David-PC-Linux>
## Created: 2017-04-02

function [retval] = resultToHist (raw, titleString)

results= raw(:,2)';
bins = [2 3 4 5 6 7 8 9 10];
hist(results , bins);
xlabel("Victory Points");
ylabel("Frequency");
numResults = length(results);
axis([2 10 0 numResults/2]);
title(titleString);
avg = mean(results);
dev = std(results);
text(7.5, 20, ["Mean: "  num2str(avg)]);
text(7.5, 19, ["Standard Dev.: "  num2str(dev)]); 

endfunction
